package com.stdsolutions.deltam.files.list;

import com.stdsolutions.deltam.files.FileList;
import com.stdsolutions.deltam.files.MigrationPath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * FileList implementation for filesystem-based resources.
 * Handles both:
 * - Real filesystem paths (filesystem: prefix) - searches actual filesystem directories
 * - Exploded JAR resources (file:// protocol) - searches classpath resources
 */
public class FileSystemBasedFileList implements FileList {

    private final MigrationPath migrationPath;

    public FileSystemBasedFileList(MigrationPath migrationPath) {
        this.migrationPath = migrationPath;
    }

    @Override
    public List<String> values() {
        // Check if this is a real filesystem path (with filesystem: prefix)
        if (migrationPath.isFileSystem()) {
            return findMigrationFilesInRealFileSystem();
        } else {
            // This is a classpath resource with file:// protocol (exploded JAR)
            return findMigrationFilesFromClasspath();
        }
    }
    
    private List<String> findMigrationFilesFromClasspath() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String resourcePath = migrationPath.toString();
            URL migrationsUrl = classLoader.getResource(resourcePath);
            
            if (migrationsUrl == null) {
                return List.of();
            }
            
            URI uri = migrationsUrl.toURI();
            return findMigrationFilesInFileSystem(uri);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Failed to discover migration files from classpath filesystem resources", e);
        }
    }
    
    private List<String> findMigrationFilesInRealFileSystem() {
        try {
            String pathString = migrationPath.toString();
            Path migrationDir = Paths.get(pathString).toAbsolutePath();
            
            if (!Files.exists(migrationDir) || !Files.isDirectory(migrationDir)) {
                return List.of();
            }
            
            List<String> files = new ArrayList<>();
            try (Stream<Path> paths = Files.list(migrationDir)) {
                files = paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
            }
            
            return files;
        } catch (IOException e) {
            throw new RuntimeException("Failed to discover migration files from real filesystem: " + migrationPath, e);
        }
    }

    private List<String> findMigrationFilesInFileSystem(URI uri) throws IOException {
        List<String> files = new ArrayList<>();
        Path migrationDir = Paths.get(uri);

        if (Files.exists(migrationDir) && Files.isDirectory(migrationDir)) {
            try (Stream<Path> paths = Files.list(migrationDir)) {
                files = paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
            }
        }

        return files;
    }
}