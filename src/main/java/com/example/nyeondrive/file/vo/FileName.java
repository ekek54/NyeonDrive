package com.example.nyeondrive.file.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
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

    public static FileName generateTmpFileName() {
        UUID randomName = UUID.randomUUID();
        return new FileName(randomName.toString());
    }

    public static FileName todayTmpFolderName() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = currentDate.format(formatter);
        return new FileName("tmp_" + formattedDate);
    }

    @Override
    public String toString() {
        if (extension.isEmpty()) {
            return this.name;
        }
        return this.name + "." + this.extension;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FileName fileName)) {
            return false;
        }
        return this.name.equals(fileName.name) && this.extension.equals(fileName.extension);
    }
}
