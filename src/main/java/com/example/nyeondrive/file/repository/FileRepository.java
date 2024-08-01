package com.example.nyeondrive.file.repository;

import com.example.nyeondrive.file.entity.File;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
    @NonNull Optional<File> findById(@NonNull Long fileId);
    Optional<File> findByOwnerIdAndContentType(UUID ownerId, String contentType);

    Optional<File> findWithAncestorClosuresById(Long fileId);

    Optional<File> findWithDescendantClosuresById(Long fileId);

    Optional<File> findWithAncestorClosuresAndDescendantClosuresById(Long fileId);
}
