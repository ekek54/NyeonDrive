package com.example.nyeondrive.controller;

public record FileFilterDto(
    String name,
    Long parentId,
    String contentType,
    Boolean isTrashed
) {
}
