package com.example.nyeondrive.file.repository;

import com.example.nyeondrive.file.dto.service.FileFilterDto;
import com.example.nyeondrive.file.dto.service.FileOrderDto;
import com.example.nyeondrive.file.dto.service.FilePagingDto;
import com.example.nyeondrive.file.entity.File;
import java.util.List;
import java.util.UUID;

public interface FileRepositoryCustom {
    List<File> findAll(FileFilterDto fileFilterDto, FilePagingDto filePagingDto,
                       List<FileOrderDto> fileOrderDtos, UUID userId);
}
