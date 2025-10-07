package com.stdsolutions.deltam.files.list;

import com.stdsolutions.deltam.files.FileList;
import com.stdsolutions.deltam.files.path.PathOf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileListImpl implements FileList {

    private final PathOf pathOf;

    public FileListImpl(PathOf pathOf) {
        this.pathOf = pathOf;
    }

    @Override
    public List<String> values() throws IOException {

        Path path = pathOf.value();
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