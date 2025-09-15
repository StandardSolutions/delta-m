package com.stdsolutions.deltam.files;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface FileList {
    List<String> values() throws IOException, URISyntaxException;
}