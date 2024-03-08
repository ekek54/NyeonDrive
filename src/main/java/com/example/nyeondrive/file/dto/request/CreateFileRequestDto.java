package com.example.nyeondrive.file.dto.request;

import com.example.nyeondrive.file.dto.service.CreateFileDto;
import jakarta.validation.constraints.NotNull;

public record CreateFileRequestDto(
        @NotNull
        String name,
        @NotNull
        Long parentId,
        @NotNull
        String contentType,
        Boolean isTrashed
) {
    public CreateFileDto toCreateFileDto() {
        return new CreateFileDto(
                name,
                parentId,
                contentType,
                isTrashedIfNullIsFalse()
        );
    }

    private boolean isTrashedIfNullIsFalse() {
        if (isTrashed == null) {
            return false;
        }
        return isTrashed;
    }
}
