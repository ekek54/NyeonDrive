package com.example.nyeondrive.repository;

import com.example.nyeondrive.dto.service.FileFilterDto;
import com.example.nyeondrive.dto.service.FilePagingDto;
import com.example.nyeondrive.entity.File;
import java.util.List;

public interface FileRepositoryCustom {
    List<File> findAllWithFilterAndPaging(FileFilterDto fileFilterDto, FilePagingDto filePagingDto);
}
