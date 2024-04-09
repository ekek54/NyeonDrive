package com.example.nyeondrive.file.dto.service;

public record FileFilterDto(
    String name,
    Long parentId,
    String contentType,
    Boolean isTrashed
) {
}
