package com.example.nyeondrive.dto.request;

import lombok.Getter;

@Getter
public class UpdateFileRequestDto {
    private String name;
    private Long parentId;
    private String contentType;
    private Boolean isTrashed;
}
