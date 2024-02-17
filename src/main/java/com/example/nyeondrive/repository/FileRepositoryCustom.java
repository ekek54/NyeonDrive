package com.example.nyeondrive.repository;

import com.example.nyeondrive.controller.FileFilterDto;
import com.example.nyeondrive.entity.File;
import java.util.List;

public interface FileRepositoryCustom {
    List<File> findAllWithFilter(FileFilterDto fileFilterDto);
}
