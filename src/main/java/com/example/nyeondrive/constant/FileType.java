package com.example.nyeondrive.constant;

import com.example.nyeondrive.vo.FileName;

public enum FileType {
    FILE, DIRECTORY;
    public boolean isDirectory() {
        return this == DIRECTORY;
    }

    public boolean isFile() {
        return this == FILE;
    }

    public static FileType of(FileName name) {
        return name.isDirectory() ? DIRECTORY : FILE;
    }
}
