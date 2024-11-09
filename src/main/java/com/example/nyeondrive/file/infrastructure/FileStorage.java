package com.example.nyeondrive.file.infrastructure;

import com.example.nyeondrive.file.entity.File;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public interface FileStorage {
    void streamUpload(File file, InputStream fileStream, UUID userId);
    void createStorage(UUID userId);
}
