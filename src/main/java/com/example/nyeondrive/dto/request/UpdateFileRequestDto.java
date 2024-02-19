package com.example.nyeondrive.dto.request;

import com.example.nyeondrive.dto.service.UpdateFileDto;
import lombok.Getter;

@Getter
public class UpdateFileRequestDto {
    private String name;
    private Long parentId;
    private String contentType;
    private Boolean isTrashed;

    public UpdateFileDto toUpdateFileDto() {
        return new UpdateFileDto(
            name,
            parentId,
            contentType,
            isTrashed
        );
    }
}
