package com.stdsolutions.deltam;

import com.stdsolutions.deltam.metadata.DatabaseType;
import com.stdsolutions.deltam.migration.MigrationLoader;
import com.stdsolutions.deltam.options.MigrationOptions;

public class TestJarMigrations {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Migration Loading from JAR ===");
        
        try {
            // Default path
            System.out.println("\n--- Default migration path ---");
            MigrationOptions defaultOptions = new MigrationOptions();
            System.out.println("Default path: " + defaultOptions.migrationPath());
            testMigrationLoading(defaultOptions, DatabaseType.H2, "H2 Default");
            
            // Custom classpath path
            System.out.println("\n--- Custom classpath path ---");
            MigrationOptions classpathOptions = new MigrationOptions("--migration-path=classpath:db/delta-m");
            System.out.println("Classpath path: " + classpathOptions.migrationPath());
            testMigrationLoading(classpathOptions, DatabaseType.H2, "H2 Classpath Custom");
            
            // External filesystem path (if available)
            System.out.println("\n--- External filesystem path ---");  
            MigrationOptions filesystemOptions = new MigrationOptions("--migration-path=filesystem:db/delta-q");
            System.out.println("Filesystem path: " + filesystemOptions.migrationPath());
            testMigrationLoading(filesystemOptions, DatabaseType.H2, "H2 Filesystem");
            testMigrationLoading(filesystemOptions, DatabaseType.POSTGRESQL, "PostgreSQL Filesystem");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testMigrationLoading(MigrationOptions options, DatabaseType dbType, String label) {
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