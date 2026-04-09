<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: modified manually.
 */
final class Version20260408203117 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Clean old indexes/foreign keys without re-adding formation.updated_at';
    }

    public function up(Schema $schema): void
    {
        // formation.updated_at already exists, so do not add it again.
    }

    public function down(Schema $schema): void
    {
        // Intentionally left empty because up() does nothing now.
    }
}