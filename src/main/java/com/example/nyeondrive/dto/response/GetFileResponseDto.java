package com.example.nyeondrive.dto.response;

import com.example.nyeondrive.entity.File;
import lombok.Getter;

@Getter
public class GetFileResponseDto {
    private final Long id;
    private final String name;
    private final Long parentId;
    private final String contentType;
    private final Boolean isTrashed;

    public GetFileResponseDto(Long id, String name, Long parentId, String contentType, Boolean isTrashed) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.contentType = contentType;
        this.isTrashed = isTrashed;
    }

    public static GetFileResponseDto of(File file) {
        return new GetFileResponseDto(file.getId(), file.getName().toString(), file.getParent().getId(), file.getContentType(), file.isTrashed());
    }
}
