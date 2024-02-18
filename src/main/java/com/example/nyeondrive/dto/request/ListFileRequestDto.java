package com.example.nyeondrive.dto.request;

import com.example.nyeondrive.dto.FileFilterDto;
import com.example.nyeondrive.dto.FilePagingDto;

public record ListFileRequestDto(
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed,
        Integer page,
        Integer size
) {
    public FilePagingDto toFilePagingDto() {
        return new FilePagingDto(page(), size());
    }

    public FileFilterDto toFileFilterDto() {
        return new FileFilterDto(name(), parentId(), contentType(), isTrashed());
    }
}
