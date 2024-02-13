package com.example.nyeondrive.dto.request;

import lombok.Getter;

@Getter
public class FileRequestDto {
    private String name;
    private Long parentId;
    private boolean isTrashed = false;
}
