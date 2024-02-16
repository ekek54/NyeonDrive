package com.example.nyeondrive.repository;

import com.example.nyeondrive.entity.File;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findById(Long fileId);

}
