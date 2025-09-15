package com.stdsolutions.deltam.files.list;

import com.stdsolutions.deltam.files.FileList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JarResourceFileList implements FileList {

    private final URL migrationsUrl;

    public JarResourceFileList(URL migrationsUrl) {
        this.migrationsUrl = migrationsUrl;
    }

    @Override
    public List<String> values() throws IOException, URISyntaxException {

        URI uri = migrationsUrl.toURI();

        if (!"jar".equals(uri.getScheme())) {
            throw new IllegalStateException("Is not jar: " + uri.getScheme());
        }


        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            String uriString = uri.toString();
            String pathStr = uriString.substring(uriString.indexOf("!/") + 2);
            Path path = fs.getPath("/" + pathStr);

            try (Stream<Path> paths = Files.list(path)) {
                return paths.filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
            }
        }

    }
}