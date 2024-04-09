package com.example.nyeondrive.file.dto.service;

public record UpdateFileDto(
    String name,
    Long parentId,
    String contentType,
    Boolean isTrashed
) {
}
