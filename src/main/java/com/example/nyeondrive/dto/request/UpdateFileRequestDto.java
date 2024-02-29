package com.example.nyeondrive.dto.request;

import com.example.nyeondrive.dto.service.UpdateFileDto;

public record UpdateFileRequestDto (
    String name,
    Long parentId,
    String contentType,
    Boolean isTrashed
) {
    public UpdateFileDto toUpdateFileDto() {
        return new UpdateFileDto(
            name,
            parentId,
            contentType,
            isTrashed
        );
    }
}
