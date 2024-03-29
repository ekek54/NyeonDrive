package com.example.nyeondrive.dto.request;

import com.example.nyeondrive.dto.service.FileFilterDto;
import com.example.nyeondrive.dto.service.FileOrderDto;
import com.example.nyeondrive.dto.service.FilePagingDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record ListFileRequestDto(
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed,
        @Min(0)
        Integer page,
        @Min(1)
        Integer size,
        @Pattern(regexp = ORDER_BY_PATTERN)
        List<String> orderBy
) {
    private static final String ORDER_BY_PATTERN = "^[a-zA-Z]*( (asc|desc))?$";

    public FilePagingDto toFilePagingDto() {
        return new FilePagingDto(page(), size());
    }

    public FileFilterDto toFileFilterDto() {
        return new FileFilterDto(name(), parentId(), contentType(), isTrashed());
    }

    public List<FileOrderDto> toFileOrderDtos() {
        if (orderBy() == null) {
            return List.of();
        }
        return orderBy().stream()
                .map(this::toFileOrderDto)
                .toList();
    }

    private FileOrderDto toFileOrderDto(String orderBy) {
        String[] split = orderBy.split(" ");
        if (split.length == 1) {
            return new FileOrderDto(split[0], "asc");
        }
        return new FileOrderDto(split[0], split[1]);
    }
}
