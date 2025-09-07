import com.stdsolutions.deltam.files.FileListOf;
import com.stdsolutions.deltam.files.path.SafePath;
import com.stdsolutions.deltam.files.path.UnPrefixedPath;
import com.stdsolutions.deltam.metadata.DatabaseType;
import com.stdsolutions.deltam.options.MigrationOptions;

import java.nio.file.Files;
import java.nio.file.Paths;

public class DebugFilesystemPath {
    public static void main(String[] args) {
        System.out.println("=== Debug Filesystem Path ===");
        
        try {
            MigrationOptions options = new MigrationOptions("--migration-path=filesystem:db/delta-q");
            System.out.println("Base migration path: " + options.migrationPath());
            
            // Simulate what MigrationLoader does
            String basePath = options.migrationPath().toString();
            String dbTypePath = DatabaseType.H2.name().toLowerCase();
            String fullPath = basePath + "/" + dbTypePath;
            
            System.out.println("Full H2 path: " + fullPath);
            System.out.println("Path exists: " + Files.exists(Paths.get(fullPath)));
            System.out.println("Current directory: " + System.getProperty("user.dir"));
            
            // Test with the actual path that MigrationLoader would use
            var migrationPath = new SafePath(new UnPrefixedPath(fullPath));
            System.out.println("Migration path toString: " + migrationPath.toString());
            System.out.println("Migration path isFileSystem: " + migrationPath.isFileSystem());
            
            var fileList = new FileListOf(migrationPath).value();
            var files = fileList.values();
            
            System.out.println("Found files: " + files.size());
            for (String file : files) {
                System.out.println("  - " + file);
            }
            
            // List actual files in directory
            var path = Paths.get(fullPath);
            if (Files.exists(path)) {
                System.out.println("Actual files in directory:");
                Files.list(path).forEach(p -> System.out.println("  - " + p.getFileName()));
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}