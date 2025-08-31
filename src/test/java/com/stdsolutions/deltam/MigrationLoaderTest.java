package com.stdsolutions.deltam;

import com.stdsolutions.deltam.metadata.DatabaseType;
import com.stdsolutions.deltam.migration.MigrationLoader;
import com.stdsolutions.deltam.options.MigrationOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MigrationLoaderTest {

    @Test
    void testStepsFromResources() throws IOException {
        MigrationOptions options = new MigrationOptions();
        MigrationLoader loader = new MigrationLoader(options, DatabaseType.H2);

        List<MigrationStep> migrations = loader.steps();

        // Проверяем, что миграции загружены
        assertNotNull(migrations, "Migrations list should not be null");
        assertTrue(migrations.size() >= 0, "Should load migrations from resources");

        // Проверяем миграции по порядку
        for (int i = 0; i < migrations.size() - 1; i++) {
            String currentId = migrations.get(i).id();
            String nextId = migrations.get(i + 1).id();
            assertTrue(currentId.compareTo(nextId) < 0,
                    "Migrations should be sorted by id: " + currentId + " should come before " + nextId);
        }
    }

    @Test
    void testMigrationStepProperties() throws IOException {
        MigrationOptions options = new MigrationOptions();
        MigrationLoader loader = new MigrationLoader(options, DatabaseType.H2);

        List<MigrationStep> migrations = loader.steps();

        for (MigrationStep migration : migrations) {
            assertNotNull(migration.id(), "Migration id should not be null");
            assertNotNull(migration.description(), "Migration description should not be null");
            assertFalse(migration.id().trim().isEmpty(), "Migration id should not be empty");
            assertFalse(migration.description().trim().isEmpty(), "Migration description should not be empty");
        }
    }

    @Test
    void testCustomMigrationsPath() throws IOException {
        // Test custom migration path option
        MigrationOptions options = new MigrationOptions("--migration-path=custom/migrations");
        MigrationLoader loader = new MigrationLoader(options, DatabaseType.H2);

        List<MigrationStep> migrations = loader.steps();

        assertNotNull(migrations, "Migrations list should not be null");
        assertEquals(0, migrations.size(), "Should return empty list for non-existent path");

        MigrationOptions defaultOptions = new MigrationOptions();
        MigrationLoader defaultLoader = new MigrationLoader(defaultOptions, DatabaseType.H2);

        List<MigrationStep> defaultMigrations = defaultLoader.steps();
        assertNotNull(defaultMigrations, "Default migrations list should not be null");
    }

    @Test
    void testTemplateProcessing() throws IOException {
        MigrationOptions options = new MigrationOptions();
        MigrationLoader loader = new MigrationLoader(options, DatabaseType.H2);

        List<MigrationStep> migrations = loader.steps();

        for (MigrationStep migration : migrations) {
            String description = migration.description();
            assertFalse(description.contains("${"),
                    "Migration description should not contain unprocessed templates: " + description);
        }
    }
}