package com.example.nyeondrive.entity;

import com.example.nyeondrive.vo.FileName;
import com.example.nyeondrive.constant.FileType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.servlet.http.Part;
import java.io.InputStream;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class File {
    @Id
    @GeneratedValue
    @Column(name = "file_id")
    private Long id;

    @Embedded
    private FileName name;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long size;

    @JoinColumn(name = "parent_id")
    @ManyToOne
    private File parent;

    @OneToMany(mappedBy = "parent")

    private List<File> children;

    @Transient
    private InputStream inputStream;

    @Builder
    public File(String fileName, String contentType, Long size, File parent, InputStream inputStream) {
        this.name = new FileName(fileName);
        this.contentType = contentType;
        this.size = size;
        this.parent = parent;
        this.inputStream = inputStream;
    }

    public FileType getFileType() {
        return FileType.of(name);
    }
    public boolean parentIsDirectory() {
        return parent.getFileType().isDirectory();
    }
}
