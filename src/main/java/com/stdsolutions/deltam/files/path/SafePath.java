package com.stdsolutions.deltam.files.path;

import com.stdsolutions.deltam.files.MigrationPath;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A secure path implementation that validates and sanitizes file paths to prevent security vulnerabilities.
 *
 * <p>This class provides protection against:
 * <ul>
 *   <li>Path traversal attacks (e.g., "../../../etc/passwd")</li>
 *   <li>Absolute path access (e.g., "/etc/passwd")</li>
 *   <li>Empty or blank paths</li>
 * </ul>
 *
 * <p>All paths are normalized and converted to use forward slashes for consistency.
 * Only relative paths within the current directory tree are allowed.
 */
public class SafePath implements MigrationPath {

    private final MigrationPath value;

    /**
     * Creates a new SafePath from the given string path.
     *
     * @param migrationPath the path string to validate and sanitize
     * @throws IllegalArgumentException if the path is null, blank, contains traversal patterns, or is absolute
     */
    public SafePath(final MigrationPath migrationPath) {
        checkEmptyPath(migrationPath.toString());
        checkTraversalPattern(migrationPath.toString());
        Path path = Paths.get(migrationPath.toString()).normalize();
        checkAbsolutePath(path);
        this.value = migrationPath;
    }

    /**
     * Returns the validated path.
     *
     * @return the safe path.
     */
    @Override
    public Path value() {
        return this.value.value();
    }

    @Override
    public String toString() {
        return this.value.toString()
                .trim()
                .replace("\\", "/")
                .replaceAll("/+", "/");
    }

    @Override
    public boolean isClasspath() {
        return this.value.isClasspath();
    }

    @Override
    public boolean isFileSystem() {
        return this.value.isFileSystem();
    }

    private static void checkEmptyPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        if (path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be blank");
        }
    }

    private static void checkTraversalPattern(String path) {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Path contains unsafe traversal patterns: " + path);
        }
    }

    private static void checkAbsolutePath(Path path) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("Absolute paths are not allowed: " + path);
        }
    }
}