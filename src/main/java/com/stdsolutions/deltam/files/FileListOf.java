package com.stdsolutions.deltam.files;

import com.stdsolutions.deltam.files.list.FileSystemBasedFileList;
import com.stdsolutions.deltam.files.list.JarResourceFileList;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

public final class FileListOf {
    private final MigrationPath path;

    public FileListOf(MigrationPath path) {
        this.path = path;
    }

    public FileList value() throws IOException {
        if (path.isFileSystem()) {
            return new FileSystemBasedFileList(path);
        }

        if (path.isClasspath()) {
            String resourcePath = path.toString();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(resourcePath);

            URL migrationsUrl = null;
            if (resources.hasMoreElements()) {
                migrationsUrl = resources.nextElement();
            }

            if (resources.hasMoreElements()) {
                throw new IllegalStateException("Multiple resources found for path: " + resourcePath);
            }

            if (migrationsUrl == null) {
                return List::of;
            }


            return switch (migrationsUrl.getProtocol()) {
                case "file" -> new FileSystemBasedFileList(path);
                case "jar" -> new JarResourceFileList(migrationsUrl);
                default -> throw new IllegalStateException("Unsupported protocol: " + migrationsUrl.getProtocol());
            };
        }
        throw new IllegalStateException("Path must contain a valid prefix (filesystem: or classpath:): " + path);
    }


}