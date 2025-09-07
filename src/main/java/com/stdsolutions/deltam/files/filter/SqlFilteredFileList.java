package com.stdsolutions.deltam.files.filter;

import com.stdsolutions.deltam.files.FileList;

import java.util.List;

public class SqlFilteredFileList implements FileList {

    private final FileList fileList;

    public SqlFilteredFileList(FileList fileList) {
        this.fileList = fileList;
    }

    @Override
    public List<String> values() {
        return fileList.values()
                .stream()
                .filter(path -> path.endsWith(".sql"))
                .toList();
    }
}