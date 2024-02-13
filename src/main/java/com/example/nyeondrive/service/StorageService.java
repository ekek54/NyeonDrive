package com.example.nyeondrive.service;

import com.example.nyeondrive.entity.File;
import org.springframework.stereotype.Service;

@Service
public interface StorageService {
    void uploadFile(File file);
}
