package com.example.nyeondrive.repository;

import com.example.nyeondrive.dto.FileFilterDto;
import com.example.nyeondrive.dto.FilePagingDto;
import com.example.nyeondrive.entity.File;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface FileRepositoryCustom {
    List<File> findAllWithFilterAndPaging(FileFilterDto fileFilterDto, FilePagingDto filePagingDto);
}
