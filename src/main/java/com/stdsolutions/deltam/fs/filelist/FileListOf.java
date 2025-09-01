package com.stdsolutions.deltam.fs.filelist;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileListOf {
    private final String path;

    public FileListOf(String path) {
        this.path = path;
    }

    public final FileList value() {
        if (Files.isDirectory(Paths.get(path))) {
            return new FileSystemFileList();
        }
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL resource = cl.getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Resource not found: " + path);
        }
        return switch (resource.getProtocol()) {
            case "file" -> new ExplodedResourceFileList();
            case "jar" -> new JarResourceFileList();
            default -> throw new IllegalStateException("Unsupported protocol: " + resource.getProtocol());
        };
    }
}