package com.example.nyeondrive.dto.request;

import com.example.nyeondrive.dto.service.FileFilterDto;
import com.example.nyeondrive.dto.service.FileOrderDto;
import com.example.nyeondrive.dto.service.FilePagingDto;
import java.util.List;
import java.util.Set;

public record ListFileRequestDto(
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed,
        Integer page,
        Integer size,
        List<String> orderBy
) {
    public static Set<String> allowedOrderBy = Set.of("id", "name", "extension", "size", "isTrashed");

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
        if (!allowedOrderBy.contains(split[0])) {
            throw new IllegalArgumentException("Invalid orderBy");
        }
        if (split.length == 1) {
            return new FileOrderDto(split[0], "asc");
        }
        return new FileOrderDto(split[0], split[1]);
    }
}
