package com.example.nyeondrive.dto.service;

import com.example.nyeondrive.dto.request.ListFileRequestDto;

public record FilePagingDto(
        Integer page,
        Integer size
) {
}
