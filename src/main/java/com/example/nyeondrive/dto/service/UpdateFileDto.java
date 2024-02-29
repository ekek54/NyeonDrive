package com.example.nyeondrive.dto.service;

public record UpdateFileDto(
    String name,
    Long parentId,
    String contentType,
    Boolean isTrashed
) {
}
