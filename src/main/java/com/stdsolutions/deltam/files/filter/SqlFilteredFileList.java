package com.stdsolutions.deltam.files.filter;

import com.stdsolutions.deltam.files.FileList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class SqlFilteredFileList implements FileList {

    private final FileList fileList;

    public SqlFilteredFileList(FileList fileList) {
        this.fileList = fileList;
    }

    @Override
    public List<String> values() throws IOException, URISyntaxException {
        return fileList.values()
                .stream()
                .filter(path -> path.endsWith(".sql"))
                .toList();
    }
}