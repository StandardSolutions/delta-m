package com.stdsolutions.deltam;

import com.stdsolutions.deltam.metadata.DatabaseType;
import com.stdsolutions.deltam.migration.MigrationLoader;
import com.stdsolutions.deltam.options.MigrationOptions;

public final class MigrationTestUtils {

    private MigrationTestUtils() {
        // Utility class
    }

    public static void testMigrationLoading(MigrationOptions options, DatabaseType dbType, String label) {
        try {
            MigrationLoader loader = new MigrationLoader(options, dbType);
            var migrations = loader.steps();

            System.out.println(label + " - Found " + migrations.size() + " migrations:");
            for (var migration : migrations) {
                System.out.println("  - " + migration.id() + ": " + migration.description());
            }

        } catch (Exception e) {
            System.out.println(label + " - Error: " + e.getMessage());
        }
    }
}