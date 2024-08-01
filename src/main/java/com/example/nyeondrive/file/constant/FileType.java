package com.example.nyeondrive.file.constant;

import java.util.List;

public enum FileType {
    FILE, FOLDER, DRIVE;
    private static final String DRIVE_CONTENT_TYPE = "drive";
    private static final String FOLDER_CONTENT_TYPE = "folder";
    private static final String FILE_CONTENT_TYPE = "file";
    private static final List<String> directoryContentTypes = List.of(DRIVE_CONTENT_TYPE, FOLDER_CONTENT_TYPE);

    public boolean isDirectory() {
        return this == FOLDER || this == DRIVE;
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
