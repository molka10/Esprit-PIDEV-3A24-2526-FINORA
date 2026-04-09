<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: modified manually.
 */
final class Version20260408231055 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Skip duplicate lesson.updated_at migration';
    }

    public function up(Schema $schema): void
    {
        // lesson.updated_at already exists
    }

    public function down(Schema $schema): void
    {
        // no-op
    }
}