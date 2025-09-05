package com.stdsolutions.deltam.files.path;

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
 *
 */
public class SafePath implements StrPath {

    private final String value;

    /**
     * Creates a new SafePath from the given string path.
     * 
     * @param strPath the path string to validate and sanitize
     * @throws IllegalArgumentException if the path is null, blank, contains traversal patterns, or is absolute
     */
    public SafePath(final String strPath) {
        checkEmptyPath(strPath);
        checkTraversalPattern(strPath);
        Path path = Paths.get(strPath).normalize();
        checkAbsolutePath(path);
        this.value = path.toString()
                .trim()
                .replace("\\", "/")
                .replaceAll("/+", "/");
    }

    /**
     * Returns the validated and sanitized path string.
     * 
     * @return the safe path string with normalized separators
     */
    @Override
    public String value() {
        return this.value;
    }

    private static void checkEmptyPath(String strPath) {
        if (strPath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        if (strPath.isBlank()) {
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