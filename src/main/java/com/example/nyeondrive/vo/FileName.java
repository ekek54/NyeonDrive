package com.example.nyeondrive.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FileName {
    @Column(name = "file_name")
    private String name;
    @Column(name = "file_extension")
    private String extension;

    public FileName(String fileName) {
        String[] split = fileName.split("\\.");
        if (split.length < 2) {
            this.name = fileName;
            this.extension = "";
        } else {
            this.name = split[0];
            this.extension = split[1];
        }
    }

    @Override
    public String toString() {
        if (extension.isEmpty()) {
            return this.name;
        }
        return this.name + "." + this.extension;
    }
}
