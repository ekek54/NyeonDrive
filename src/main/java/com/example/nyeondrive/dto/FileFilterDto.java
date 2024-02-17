package com.example.nyeondrive.dto;

public record FileFilterDto(
    String name,
    Long parentId,
    String contentType,
    Boolean isTrashed
) {
}
