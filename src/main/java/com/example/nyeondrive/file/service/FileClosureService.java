package com.example.nyeondrive.file.service;

import com.example.nyeondrive.file.entity.FileClosure;
import com.example.nyeondrive.file.repository.FileClosureRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileClosureService {
    private final FileClosureRepository fileClosureRepository;

    public List<FileClosure> findChildrenClosureWithDescendant(Long fileId) {
        return fileClosureRepository.findAllByAncestor_idAndDepthWithDescendant(fileId, 1L);
    }


}
