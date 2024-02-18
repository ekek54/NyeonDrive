package com.example.nyeondrive.dto;

import com.example.nyeondrive.dto.request.ListFileRequestDto;

public record FilePagingDto(
        Integer page,
        Integer size
) {
}
