package com.example.nyeondrive.file.service;

import com.example.nyeondrive.file.entity.File;
import org.springframework.stereotype.Service;

@Service
public interface StorageService {
    void uploadFile(File file);
}
