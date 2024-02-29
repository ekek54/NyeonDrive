package com.example.nyeondrive.constant;

import java.util.List;

public enum FileType {
    FILE, DIRECTORY;
    private static final List<String> directoryContentTypes = List.of("directory", "drive", "folder");

    public boolean isDirectory() {
        return this == DIRECTORY;
    }

    public boolean isFile() {
        return this == FILE;
    }

    public static FileType of(String contentType) {
        if (directoryContentTypes.contains(contentType)) {
            return DIRECTORY;
        }
        return FILE;
    }
}
