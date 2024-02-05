package com.example.nyeondrive.controller;

import com.example.nyeondrive.entity.File;
import com.example.nyeondrive.repository.FileRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileRepository fileRepository;

    public FileController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }


    /**
     * 단일 파일 업로드에 사용
     * 파일의 스트림을 그대로 스토리지로 전달
     * @return File
     */
    @PostMapping(value = "/upload", params = "mode=stream")
    public String streamUpload(HttpServletRequest request) throws IOException, ServletException {
        String contentType = request.getContentType();
        Long contentLength = (long) request.getContentLength();
        String fileName = request.getHeader("File-Name");
        String relativePath = request.getHeader("Relative-Path");
        File file = new File(fileName, contentType, contentLength, request.getInputStream());
        fileRepository.uploadFile(file);
        return "streamUpload";
    }

}
