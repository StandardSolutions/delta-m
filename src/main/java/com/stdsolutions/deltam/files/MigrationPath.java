package com.stdsolutions.deltam.files;

import java.nio.file.Path;

public interface MigrationPath {

    Path value();

    boolean isClasspath();

    boolean isFileSystem();
}