package com.example.nyeondrive.file.repository;

import com.example.nyeondrive.file.entity.File;
import com.example.nyeondrive.file.vo.FileName;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FileRepository extends JpaRepository<File, Long>, FileRepositoryCustom {
    @NonNull
    Optional<File> findById(@NonNull Long fileId);

    Optional<File> findByOwnerIdAndContentType(UUID ownerId, String contentType);

    @Query("SELECT f FROM File f JOIN FETCH f.ancestorClosures WHERE f.id = :fileId")
    Optional<File> findWithAncestorClosuresById(Long fileId);

    @Query("select f from File f inner join f.descendantClosures descendantClosures where descendantClosures.descendant = ?1")
    List<File> findAllByDescendant(File file);

    @Query("select f from File f join fetch f.ancestorClosures where f.id in ?1")
    List<File> findAllWithAncestorClosuresByIdIn(Collection<Long> id);

    List<File> findAllByIdIn(List<Long> descendantIds);

    Optional<File> findByFileNameAndOwnerId(FileName fileName, UUID ownerId);
}