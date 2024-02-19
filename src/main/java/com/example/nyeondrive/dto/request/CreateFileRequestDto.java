package com.example.nyeondrive.dto.request;

import com.example.nyeondrive.dto.service.CreateFileDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateFileRequestDto {
    @NotNull
    private String name;
    @NotNull
    private Long parentId;
    @NotNull
    private String contentType;
    private boolean isTrashed;

    public CreateFileDto toCreateFileDto() {
        return new CreateFileDto(
            name,
            parentId,
            contentType,
            isTrashed
        );
    }
}
