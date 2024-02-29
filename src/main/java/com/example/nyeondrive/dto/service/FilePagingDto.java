package com.example.nyeondrive.dto.service;

public record FilePagingDto(
        Integer page,
        Integer size
) {
    public Boolean isEmpty() {
        return page == null || size == null;
    }
}
