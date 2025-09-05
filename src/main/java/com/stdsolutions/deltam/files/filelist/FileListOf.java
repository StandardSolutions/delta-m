package com.stdsolutions.deltam.files.filelist;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class FileListOf {
    private final String path;

    public FileListOf(String path) {
        this.path = path;
    }

    public FileList value() {
        if (path.startsWith("filesystem:")) {
            return new FileSystemFileList();
        }
        
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            List<URL> migrationResources = findMigrationResources(resourcePath);
            if (migrationResources.isEmpty()) {
                throw new IllegalStateException("No migration resources found: " + resourcePath);
            }
            
            URL primaryResource = migrationResources.get(0);
            return switch (primaryResource.getProtocol()) {
                case "file" -> new ExplodedResourceFileList();
                case "jar" -> new JarResourceFileList();
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