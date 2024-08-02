package com.example.nyeondrive.file.repository;

import com.example.nyeondrive.file.entity.File;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FileRepository extends JpaRepository<File, Long> {
    @NonNull Optional<File> findById(@NonNull Long fileId);
    Optional<File> findByOwnerIdAndContentType(UUID ownerId, String contentType);

    @Query("SELECT f FROM File f JOIN FETCH f.ancestorClosures WHERE f.id = :fileId")
    Optional<File> findWithAncestorClosuresById(Long fileId);

    Optional<File> findWithDescendantClosuresById(Long fileId);

    Optional<File> findWithAncestorClosuresAndDescendantClosuresById(Long fileId);
}
