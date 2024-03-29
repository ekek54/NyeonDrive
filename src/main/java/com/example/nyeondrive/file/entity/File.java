package com.example.nyeondrive.file.entity;

import com.example.nyeondrive.file.vo.FileName;
import com.example.nyeondrive.file.constant.FileType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Embedded
    private FileName fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long size;

    @Column(name = "is_trashed")
    private boolean isTrashed = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JoinColumn(name = "parent_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private File parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<File> children;

    @Transient
    private InputStream inputStream;

    @Builder
    public File(String fileName, String contentType, Long size, File parent, InputStream inputStream,
                boolean isTrashed) {
        this.fileName = new FileName(fileName);
        this.contentType = contentType;
        this.size = size;
        if (parent.isFile()) {
            throw new RuntimeException("Parent is not a directory");
        }
        this.parent = parent;
        this.inputStream = inputStream;
        this.isTrashed = isTrashed;
    }

    public static File createRootFolder() {
        File file = new File();
        file.setFileName("root");
        file.setContentType("drive");
        return file;
    }

    public void setFileName(String fileName) {
        this.fileName = new FileName(fileName);
    }

    public void setParent(File parent) {
        if (parent.isFile()) {
            throw new RuntimeException("Parent is not a directory");
        }
        this.parent = parent;
    }

    public boolean isFile() {
        return FileType.of(contentType) == FileType.FILE;
    }

}
