package com.stdsolutions.deltam.files.list;

import com.stdsolutions.deltam.files.FileList;
import com.stdsolutions.deltam.files.MigrationPath;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarResourceFileList implements FileList {

    private final MigrationPath migrationPath;

    public JarResourceFileList(MigrationPath migrationPath) {
        this.migrationPath = migrationPath;
    }

    @Override
    public List<String> values() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String resourcePath = migrationPath.toString();
            URL migrationsUrl = classLoader.getResource(resourcePath);
            
            if (migrationsUrl == null) {
                return List.of();
            }
            
            return findMigrationFilesInJar(migrationsUrl, resourcePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to discover migration files from JAR", e);
        }
    }

    private List<String> findMigrationFilesInJar(URL resource, String migrationsPath) throws IOException {
        List<String> files = new ArrayList<>();
        JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();

        try (JarFile jarFile = jarConnection.getJarFile()) {
            String pathWithSlash = migrationsPath.endsWith("/") ? migrationsPath : migrationsPath + "/";

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(pathWithSlash) &&
                    !entry.isDirectory()) {

                    String fileName = entryName.substring(pathWithSlash.length());
                    if (!fileName.contains("/")) { // Only direct files, not subdirectories
                        files.add(fileName);
                    }
                }
            }
        }

        return files;
    }
}