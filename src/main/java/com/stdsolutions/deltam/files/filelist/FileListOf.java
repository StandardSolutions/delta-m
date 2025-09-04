package com.stdsolutions.deltam.files.filelist;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileListOf {
    private final String path;

    public FileListOf(String path) {
        this.path = path;
    }

    public final FileList value() {
        if (path.startsWith("filesystem:")) {
            return new FileSystemFileList();
        }
        
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL resource = cl.getResource(resourcePath);
            if (resource == null) {
                throw new IllegalStateException("Resource not found: " + resourcePath);
            }
            return switch (resource.getProtocol()) {
                case "file" -> new ExplodedResourceFileList();
                case "jar" -> new JarResourceFileList();
                default -> throw new IllegalStateException("Unsupported protocol: " + resource.getProtocol());
            };
        }
        throw new IllegalStateException("Path must contain a valid prefix (filesystem: or classpath:): " + path);
    }
}