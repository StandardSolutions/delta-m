package com.stdsolutions.deltam.files;

import com.stdsolutions.deltam.files.list.FileSystemBasedFileList;
import com.stdsolutions.deltam.files.list.JarResourceFileList;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class FileListOf {
    private final MigrationPath path;

    public FileListOf(MigrationPath path) {
        this.path = path;
    }

    public FileList value() {
        if (path.isFileSystem()) {
            return new FileSystemBasedFileList(path);
        }
        
        // Treat both classpath: prefixed paths and unprefixed paths as classpath resources for backward compatibility
        if (path.isClasspath() || (!path.isFileSystem() && !path.isClasspath())) {
            String resourcePath = path.toString();
            List<URL> migrationResources = findMigrationResources(resourcePath);
            if (migrationResources.isEmpty()) {
                // Return empty file list instead of throwing exception for backward compatibility
                return () -> List.of();
            }
            
            URL primaryResource = migrationResources.get(0);
            return switch (primaryResource.getProtocol()) {
                case "file" -> new FileSystemBasedFileList(path);
                case "jar" -> new JarResourceFileList(path);
                default -> throw new IllegalStateException("Unsupported protocol: " + primaryResource.getProtocol());
            };
        }
        throw new IllegalStateException("Path must contain a valid prefix (filesystem: or classpath:): " + path);
    }

    private List<URL> findMigrationResources(String resourcePath) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        List<URL> migrationUrls = new ArrayList<>();
        
        try {
            Enumeration<URL> resources = cl.getResources(resourcePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (hasDeltaMMarker(url)) {
                    migrationUrls.add(url);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find migration resources for: " + resourcePath, e);
        }
        
        return migrationUrls;
    }
    
    private boolean hasDeltaMMarker(URL migrationUrl) {
        try {
            String urlString = migrationUrl.toString();
            // Попытаться найти маркерный файл в корне ресурсов
            String markerPath = urlString.replaceAll("/migrations/.*", "/META-INF/delta-m.marker");
            URL markerUrl = new URL(markerPath);
            markerUrl.openStream().close(); // Просто проверить, что файл существует
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}