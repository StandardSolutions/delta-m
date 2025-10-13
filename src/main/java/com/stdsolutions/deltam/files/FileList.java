package com.stdsolutions.deltam.files;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public interface FileList {
    List<Path> values() throws IOException, URISyntaxException;
}