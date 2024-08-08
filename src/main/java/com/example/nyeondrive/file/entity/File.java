package com.example.nyeondrive.file.entity;

import com.example.nyeondrive.file.constant.FileType;
import com.example.nyeondrive.file.vo.FileName;
import jakarta.persistence.CascadeType;
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
import java.util.ArrayList;
import java.util.Collections;
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
    private List<FileClosure> ancestorClosures = new ArrayList<>();

    @OneToMany(mappedBy = "ancestor", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<FileClosure> descendantClosures = new ArrayList<>();

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

    public List<FileClosure> getAncestorClosures() {
        return Collections.unmodifiableList(ancestorClosures);
    }

    public List<File> getAncestors() {
        return ancestorClosures.stream()
                .map(FileClosure::getAncestor)
                .toList();
    }

    public void addAncestorClosure(FileClosure fileClosure) {
        ancestorClosures.add(fileClosure);
    }

    public void removeAncestorClosure(FileClosure fileClosure) {
        ancestorClosures.remove(fileClosure);
    }

    public List<FileClosure> getDescendantClosures() {
        return Collections.unmodifiableList(descendantClosures);
    }

    public List<File> getDescendants() {
        return descendantClosures.stream()
                .map(FileClosure::getDescendant)
                .toList();
    }

    public void addDescendantClosure(FileClosure fileClosure) {
        descendantClosures.add(fileClosure);
    }

    public void removeDescendantClosure(FileClosure fileClosure) {
        descendantClosures.remove(fileClosure);
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

    public boolean isTrashed() {
        return isTrashed;
    }

    public void trash() {
        isTrashed = true;
    }

    public void restore() {
        isTrashed = false;
    }

    public boolean isAncestorTrashed() {
        return getAncestors().stream()
                .anyMatch(File::isTrashed);
    }

    public File getParent() {
        return ancestorClosures.stream()
                .filter(FileClosure::isImmediate)
                .findFirst()
                .map(FileClosure::getAncestor)
                .orElse(null);
    }

    /**
     * 파일을 포함할 수 있는지 확인
     * 부모가 폴더인지 확인
     * 순환 구조가 발생하는지 확인
     * 부모 폴더가 삭제 상태인지 확인
     * TODO: 부모 폴더내에 같은 이름의 파일이 있는지 확인
     * @param file: 이동할 파일
     * @return: 유효한 위치인지 여부
     */
    public boolean canContain(File file) {
        if (isTrashed()) {
            throw new IllegalStateException("Trashed File cannot contain another file");
        }
        if (isFile()) {
            throw new IllegalArgumentException("File cannot contain another file");
        }
        if (isAncestorTrashed()) {
            throw new IllegalStateException("Ancestor is trashed");
        }
        if (getAncestors().contains(file)) {
            throw new IllegalStateException("Cycle detected");
        }
        return true;
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
