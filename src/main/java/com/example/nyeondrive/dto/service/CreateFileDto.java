package com.example.nyeondrive.dto.service;

public record CreateFileDto(
    String name,
    Long parentId,
    String contentType,
    boolean isTrashed
) {
}
