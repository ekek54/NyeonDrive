package com.example.nyeondrive.repository;

import com.example.nyeondrive.entity.File;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
@Repository
@RequiredArgsConstructor
public class JpaFileRepository {
    @PersistenceContext
    private EntityManager em;


    public void saveFile(File file) {
        em.persist(file);
    }

    public Optional<File> findById(Long fileId) {
        return Optional.ofNullable(em.find(File.class, fileId));
    }
}
