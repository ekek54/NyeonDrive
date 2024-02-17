package com.example.nyeondrive.repository;

import com.example.nyeondrive.dto.FileFilterDto;
import com.example.nyeondrive.entity.File;
import java.util.List;

public interface FileRepositoryCustom {
    List<File> findAllWithFilter(FileFilterDto fileFilterDto);
}
