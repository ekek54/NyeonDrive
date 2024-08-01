package com.example.nyeondrive.file.entity;

import com.example.nyeondrive.file.constant.FileType;
import com.example.nyeondrive.file.vo.FileName;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
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

    @Column(name = "owner_id")
    private UUID ownerId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "descendant")
    private List<FileClosure> ancestorClosures;

    @OneToMany(mappedBy = "ancestor")
    private List<FileClosure> descendantClosures;

    @Transient
    private InputStream inputStream;


    @Builder
    public File(String fileName, String contentType, Long size, InputStream inputStream,
                boolean isTrashed) {
        this.fileName = new FileName(fileName);
        this.contentType = contentType;
        this.size = size;
        this.inputStream = inputStream;
        this.isTrashed = isTrashed;
    }

    public static File createDrive(UUID userId) {
        File file = new File();
        file.setOwnerId(userId);
        file.setFileName("drive");
        file.setContentType("drive");
        return file;
    }


    public void setFileName(String fileName) {
        this.fileName = new FileName(fileName);
    }

    public boolean isFile() {
        return FileType.of(contentType) == FileType.FILE;
    }

    public boolean isFolder() {
        return FileType.of(contentType) == FileType.FOLDER;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        File file = (File) o;
        return getId() != null && Objects.equals(getId(), file.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();
    }
}
