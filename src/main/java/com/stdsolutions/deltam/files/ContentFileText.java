package com.stdsolutions.deltam.files;

public class ContentFileText implements ContentFile {

    private final String fileName;

    private final String content;

    public ContentFileText(final String fileName, final String content) {
        this.fileName = fileName;
        this.content = content;
    }

    @Override
    public String fileName() {
        return this.fileName;
    }

    @Override
    public String content() {
        return this.content;
    }
}
