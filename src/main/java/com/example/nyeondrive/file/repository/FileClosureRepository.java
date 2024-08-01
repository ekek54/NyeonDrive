package com.example.nyeondrive.file.repository;

import com.example.nyeondrive.file.entity.File;
import com.example.nyeondrive.file.entity.FileClosure;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FileClosureRepository extends JpaRepository<FileClosure, Long>, FileClosureRepositoryCustom {
    List<FileClosure>findAllByDescendant_Id(Long descendantId);

    @Query("SELECT fc FROM FileClosure fc JOIN FETCH fc.descendant WHERE fc.ancestor.id = :ancestor_id AND fc.depth = :depth")
    List<FileClosure> findAllByAncestor_idAndDepthWithDescendant(Long ancestor_id, Long depth);

    Optional<FileClosure> findWithAncestorByDescendantAndDepth(File descendant, Long depth);

    List<FileClosure> findAllByDescendant(File parent);

    List<FileClosure> findAllByAncestor(File file);

    List<FileClosure> findAllByDescendantAndAncestor_Trashed(File descendant, boolean ancestor_trashed);

    @Modifying
    @Query("DELETE FROM FileClosure fc WHERE fc.ancestor IN :ancestors AND fc.descendant IN :descendants")
    void deleteAllInBatchByAncestorInAndDescendantIn(List<File> ancestors, List<File> descendants);
}
