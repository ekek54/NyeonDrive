package com.example.nyeondrive.file.dto.service;

public record FilePagingDto(
        Integer page,
        Integer size
) {
    public Boolean isEmpty() {
        return page == null || size == null;
    }
}
