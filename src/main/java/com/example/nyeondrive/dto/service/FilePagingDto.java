package com.example.nyeondrive.dto.service;

import com.example.nyeondrive.dto.request.ListFileRequestDto;

public record FilePagingDto(
        Integer page,
        Integer size
) {
    public Boolean isEmpty() {
        return page == null || size == null;
    }
}
