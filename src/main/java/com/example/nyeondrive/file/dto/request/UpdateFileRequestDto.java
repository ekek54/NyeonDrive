package com.example.nyeondrive.file.dto.request;

import com.example.nyeondrive.file.dto.service.UpdateFileDto;

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
