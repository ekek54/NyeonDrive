package com.example.nyeondrive.dto.request;

import com.example.nyeondrive.dto.service.FileFilterDto;
import com.example.nyeondrive.dto.service.FileOrderDto;
import com.example.nyeondrive.dto.service.FilePagingDto;
import java.util.List;

public record ListFileRequestDto(
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed,
        Integer page,
        Integer size,
        List<String> orderBy
) {
    public FilePagingDto toFilePagingDto() {
        return new FilePagingDto(page(), size());
    }

    public FileFilterDto toFileFilterDto() {
        return new FileFilterDto(name(), parentId(), contentType(), isTrashed());
    }
    public List<FileOrderDto> toFileOrderDtos() {
        return orderBy().stream()
                .map(this::toFilerOrderDto)
                .toList();
    }

    private FileOrderDto toFilerOrderDto(String orderBy) {
        String[] split = orderBy.split(" ");
        if (split.length == 1) {
            return new FileOrderDto(split[0], "asc");
        }
        return new FileOrderDto(split[0], split[1]);
    }
}
