package com.example.nyeondrive.dto.service;

public record FileFilterDto(
    String name,
    Long parentId,
    String contentType,
    Boolean isTrashed
) {
}
