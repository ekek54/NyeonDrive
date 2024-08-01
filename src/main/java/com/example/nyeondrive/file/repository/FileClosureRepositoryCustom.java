package com.example.nyeondrive.file.repository;

import com.example.nyeondrive.file.dto.service.FileFilterDto;
import com.example.nyeondrive.file.dto.service.FileOrderDto;
import com.example.nyeondrive.file.dto.service.FilePagingDto;
import com.example.nyeondrive.file.entity.FileClosure;
import java.util.List;

public interface FileClosureRepositoryCustom {
    List<FileClosure> findAll(FileFilterDto fileFilterDto, FilePagingDto filePagingDto,
                              List<FileOrderDto> fileOrderDtos);
}
