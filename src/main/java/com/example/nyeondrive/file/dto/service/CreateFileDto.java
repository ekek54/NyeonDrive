package com.example.nyeondrive.file.dto.service;

public record CreateFileDto(
    String name,
    Long parentId,
    String contentType,
    boolean isTrashed
) {
}
