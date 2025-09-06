package com.stdsolutions.deltam.files.path;

import java.nio.file.Path;
import java.nio.file.Paths;


public final class UnPrefixedPath implements MigrationPath {

    private final String value;

    public UnPrefixedPath(final String strPath) {
        this.value = strPath;
    }

    @Override
    public Path value() {
        return Paths.get(unPrefixedValue());
    }

    @Override
    public String toString() {
        return unPrefixedValue();
    }

    @Override
    public boolean isClasspath() {
        return this.value.startsWith("classpath:");
    }

    @Override
    public boolean isFileSystem() {
        return this.value.startsWith("filesystem:");
    }

    private String unPrefixedValue() {
        if (isClasspath()) {
            return this.value.substring("classpath:".length());
        }
        if (isFileSystem()) {
            return this.value.substring("filesystem:".length());
        }
        return value;
    }
}