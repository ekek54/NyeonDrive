package com.example.nyeondrive.repository;

import com.example.nyeondrive.controller.FileFilterDto;
import com.example.nyeondrive.dto.response.GetFileResponseDto;
import com.example.nyeondrive.entity.File;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface FileRepository extends JpaRepository<File, Long>, FileRepositoryCustom {
    @NonNull Optional<File> findById(@NonNull Long fileId);
}
