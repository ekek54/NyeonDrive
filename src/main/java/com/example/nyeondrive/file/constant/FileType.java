package com.example.nyeondrive.file.constant;

import java.util.List;

public enum FileType {
    FILE, FOLDER;
    private static final List<String> directoryContentTypes = List.of("directory", "drive", "folder");

    public boolean isDirectory() {
        return this == FOLDER;
    }

    public boolean isFile() {
        return this == FILE;
    }

    public static FileType of(String contentType) {
        if (directoryContentTypes.contains(contentType)) {
            return FOLDER;
        }
        return FILE;
    }
}
