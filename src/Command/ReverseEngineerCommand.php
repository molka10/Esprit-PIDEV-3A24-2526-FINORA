<?php

namespace App\Command;

use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:reverse-engineer',
    description: 'Crée les entités automatiquement pour les tables non mappées (Reverse Engineering)',
)]
class ReverseEngineerCommand extends Command
{
    public function __construct(private EntityManagerInterface $entityManager)
    {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $conn = $this->entityManager->getConnection();
        $schemaManager = $conn->createSchemaManager();
        $tables = $schemaManager->listTables();
        
        $entityDir = __DIR__ . '/../Entity/';
        if (!is_dir($entityDir)) {
            mkdir($entityDir, 0777, true);
        }

        // On liste les entités déjà existantes pour éviter de les écraser (comme Investment.php)
        $existingClasses = [];
        foreach (glob($entityDir . '*.php') as $file) {
            $existingClasses[] = basename($file, '.php');
        }

        foreach ($tables as $table) {
            $tableName = $table->getName();
            
            if ($tableName === 'doctrine_migration_versions' || $tableName === 'messenger_messages') {
                continue; // On ignore les tables internes Symfony
            }

            // ex: appel_offre -> AppelOffre
            $className = str_replace(' ', '', ucwords(str_replace('_', ' ', $tableName)));
            
            if (in_array($className, $existingClasses)) {
                continue;
            }

            $io->text("Génération de l'entité $className pour la table $tableName...");
            
            $fileContent = "<?php\n\nnamespace App\\Entity;\n\nuse Doctrine\\ORM\\Mapping as ORM;\n\n#[ORM\\Entity]\n#[ORM\\Table(name: '$tableName')]\nclass $className\n{\n";

            $columns = $table->getColumns();
            $primaryKeyArray = $table->hasPrimaryKey() ? $table->getPrimaryKey()->getColumns() : [];

            foreach ($columns as $column) {
                $colName = $column->getName();
                $type = $column->getType()->getName();
                
                // Mappage des types Doctrines
                if (in_array($type, ['integer', 'smallint', 'bigint'])) {
                    $doctrineType = 'integer';
                    $phpType = 'int';
                } elseif ($type === 'boolean') {
                    $doctrineType = 'boolean';
                    $phpType = 'bool';
                } elseif (in_array($type, ['datetime', 'date'])) {
                    $doctrineType = $type;
                    $phpType = '\\DateTimeInterface';
                } elseif (in_array($type, ['float', 'decimal'])) {
                    $doctrineType = 'decimal';
                    $phpType = 'string';
                } elseif ($type === 'text') {
                    $doctrineType = 'text';
                    $phpType = 'string';
                } else {
                    $doctrineType = 'string';
                    $phpType = 'string';
                }

                $nullable = !$column->getNotnull();
                if ($isPk = in_array($colName, $primaryKeyArray)) {
                    $nullable = false;
                }

                $phpPropType = ($nullable && $phpType !== 'mixed' ? '?' : '') . $phpType;
                $nullableString = $nullable ? ', nullable: true' : '';

                $idAttribute = $isPk ? "    #[ORM\\Id]\n" . ($column->getAutoincrement() ? "    #[ORM\\GeneratedValue]\n" : '') : '';

                // camelCase
                $propName = lcfirst(str_replace(' ', '', ucwords(str_replace('_', ' ', $colName))));

                $fileContent .= "$idAttribute    #[ORM\\Column(name: '$colName', type: '$doctrineType'$nullableString)]\n";
                $fileContent .= "    private $phpPropType $$propName = null;\n\n";

                // Getters & Setters
                $getterName = 'get' . ucfirst($propName);
                $setterName = 'set' . ucfirst($propName);

                $fileContent .= "    public function $getterName(): $phpPropType\n    {\n        return \$this->$propName;\n    }\n\n";
                
                // Setters (on n'ajoute pas le type hinting pour simplifier la compatibilité des nullables dans l'exemple)
                $fileContent .= "    public function $setterName($phpPropType $$propName): self\n    {\n        \$this->$propName = $$propName;\n        return \$this;\n    }\n\n";
                
                if ($isPk && $propName !== 'id') {
                    $fileContent .= "    public function getId(): $phpPropType\n    {\n        return \$this->$propName;\n    }\n\n";
                }
            }

            $fileContent .= "}\n";

            file_put_contents($entityDir . $className . '.php', $fileContent);
            $io->success("Entité : App\\Entity\\$className générée avec succès.");
        }

        $io->success("Reverse engineering terminé !! Vous pouvez maintenant lancer `php bin/console doctrine:schema:validate` =)");
        return Command::SUCCESS;
    }
}
