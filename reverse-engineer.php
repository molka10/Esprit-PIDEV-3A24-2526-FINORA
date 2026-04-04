<?php
require_once 'vendor/autoload.php';


$dbHost = '127.0.0.1';
$dbName = 'finora';
$dbUser = 'root';
$dbPass = '';
$dbPort = 3306;


$namespace = 'App\\Entity';
$outputDir = __DIR__ . '/src/Entity';

// Create output directory if it doesn't exist
if (!is_dir($outputDir)) {
    mkdir($outputDir, 0777, true);
}

// Connect to database
try {
    $pdo = new PDO("mysql:host=$dbHost;port=$dbPort;dbname=$dbName;charset=utf8mb4", $dbUser, $dbPass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    echo "Connected to database successfully!\n";
} catch (PDOException $e) {
    die("Database connection failed: " . $e->getMessage() . "\n");
}

// Get tables
$stmt = $pdo->query("SHOW TABLES");
$tables = $stmt->fetchAll(PDO::FETCH_COLUMN);

foreach ($tables as $table) {

    // Skip doctrine tables
    if (strpos($table, 'migration') !== false || strpos($table, 'doctrine') !== false) {
        continue;
    }

    echo "Generating entity for: $table\n";

    // Convert table name to class name
    $className = str_replace(' ', '', ucwords(str_replace('_', ' ', $table)));

    // Get columns
    $stmt = $pdo->query("DESCRIBE `$table`");
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $entityCode = "<?php\n\n";
    $entityCode .= "namespace $namespace;\n\n";
    $entityCode .= "use Doctrine\\ORM\\Mapping as ORM;\n\n";
    $entityCode .= "#[ORM\\Entity]\n";
    $entityCode .= "#[ORM\\Table(name: '$table')]\n";
    $entityCode .= "class $className\n{\n";

    foreach ($columns as $column) {

        $field = $column['Field'];
        $type = mapType($column['Type']);
        $nullable = $column['Null'] === 'YES' ? 'true' : 'false';

        // Primary key
        if ($column['Key'] === 'PRI') {
            $entityCode .= "    #[ORM\\Id]\n";
            $entityCode .= "    #[ORM\\GeneratedValue]\n";
            $entityCode .= "    #[ORM\\Column(type: '$type')]\n";
        } else {
            $entityCode .= "    #[ORM\\Column(type: '$type', nullable: $nullable)]\n";
        }

        $entityCode .= "    private ?$type \$$field = null;\n\n";

        // Getter
        $entityCode .= "    public function get" . ucfirst($field) . "(): ?$type\n";
        $entityCode .= "    {\n";
        $entityCode .= "        return \$this->$field;\n";
        $entityCode .= "    }\n\n";

        // Setter
        $entityCode .= "    public function set" . ucfirst($field) . "($type \$$field): self\n";
        $entityCode .= "    {\n";
        $entityCode .= "        \$this->$field = \$$field;\n";
        $entityCode .= "        return \$this;\n";
        $entityCode .= "    }\n\n";
    }

    $entityCode .= "}\n";

    file_put_contents("$outputDir/$className.php", $entityCode);
}

echo "Entities generated successfully!\n";


function mapType($mysqlType)
{
    if (strpos($mysqlType, 'int') !== false) return 'int';
    if (strpos($mysqlType, 'decimal') !== false || strpos($mysqlType, 'float') !== false) return 'float';
    if (strpos($mysqlType, 'datetime') !== false || strpos($mysqlType, 'date') !== false) return '\\DateTimeInterface';
    return 'string';
}