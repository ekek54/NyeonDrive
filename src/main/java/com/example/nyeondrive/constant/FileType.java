package com.example.nyeondrive.constant;

public enum FileType {
    FILE, DIRECTORY;
    public boolean isDirectory() {
        return this == DIRECTORY;
    }

    public boolean isFile() {
        return this == FILE;
    }

    public static FileType of(String contentType) {
        if (contentType.equals("directory") || contentType.equals("drive")) {
            return DIRECTORY;
        }
        return FILE;
    }
}
