package com.stdsolutions.deltam.files.path;

import java.nio.file.Path;

public interface MigrationPath {

    Path value();

    boolean isClasspath();

    boolean isFileSystem();
}