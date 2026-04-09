<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: modified manually.
 */
final class Version20260408231248 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Skip old index and foreign key cleanup';
    }

    public function up(Schema $schema): void
    {
        // no-op
    }

    public function down(Schema $schema): void
    {
        // no-op
    }
}