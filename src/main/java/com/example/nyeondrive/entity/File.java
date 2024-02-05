package com.example.nyeondrive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.servlet.http.Part;
import java.io.InputStream;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class File {
    @Id
    @GeneratedValue
    @Column(name = "file_id")
    private Long id;
    private String name;
    private String type;
    private Long size;
    @Transient
    private InputStream data;

    public File() {

    }

    public File(String fileName, String contentType, Long contentLength, InputStream inputStream) {
        this.name = fileName;
        this.type = contentType;
        this.size = contentLength;
        this.data = inputStream;
    }

    public static File createBySinglePart(Part part) {
        File file = new File();
        file.name = part.getSubmittedFileName();
        file.type = part.getContentType();
        file.size = part.getSize();

        try {
            file.data = part.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
