package com.stdsolutions.deltam.files.path;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;

/**
 * Converts a URI to a Path, handling both regular filesystem and JAR/ZIP resources.
 * <p>
 * This class provides a universal way to obtain a Path from a URI, automatically
 * creating and managing filesystem providers for JAR resources when necessary.
 * </p>
 */
public class PathOf {
    private final URI uri;

    /**
     * Constructs a PathOf with the specified URI.
     *
     * @param uri the URI to convert to a Path.
     */
    public PathOf(URI uri) {
        this.uri = uri;
    }

    /**
     * Converts the URI to a Path.
     * <p>
     * For regular filesystem URIs, this uses {@link Paths#get(URI)} directly.
     * For JAR resources, it creates a new filesystem if needed and obtains the path
     * from the filesystem provider.
     * </p>
     *
     * @return the Path corresponding to the URI
     * @throws IOException if an I/O error occurs while creating the filesystem
     */
    public Path value() throws IOException {
        try {
            return Paths.get(uri);
        } catch (FileSystemNotFoundException nfe) {
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                return fs.provider().getPath(uri);
            }
        }
    }
}