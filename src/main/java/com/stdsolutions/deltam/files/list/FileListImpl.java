package com.stdsolutions.deltam.files.list;

import com.stdsolutions.deltam.files.FileList;
import com.stdsolutions.deltam.files.path.PathOf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileListImpl implements FileList {

    private final URI uri;

    public FileListImpl(URI uri) {
        this.uri = uri;
    }

    public FileListImpl(URL url) throws URISyntaxException {
        this.uri = url.toURI();
    }

    @Override
    public List<String> values() throws IOException {

        Path path = new PathOf(uri).value();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(path)) {
            return paths.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }
}