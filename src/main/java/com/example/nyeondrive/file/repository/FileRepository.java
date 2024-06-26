package com.example.nyeondrive.file.repository;

import com.example.nyeondrive.file.entity.File;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long>, FileRepositoryCustom {
    @NonNull Optional<File> findById(@NonNull Long fileId);
}
