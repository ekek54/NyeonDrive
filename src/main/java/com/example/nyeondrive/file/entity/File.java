package com.example.nyeondrive.file.entity;

import com.example.nyeondrive.exception.error.BadRequestException;
import com.example.nyeondrive.exception.error.ForbiddenException;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Slf4j
@Entity
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Embedded
    @Setter
    private FileName fileName;

    @Column(name = "content_type")
    @Setter
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

    @OneToMany(mappedBy = "descendant", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<FileClosure> ancestorClosures = new ArrayList<>();

    @OneToMany(mappedBy = "ancestor")
    private final List<FileClosure> descendantClosures = new ArrayList<>();

    @Transient
    private InputStream inputStream;


    @Builder
    public File(String fileName, String contentType, Long size, InputStream inputStream,
                boolean isTrashed, UUID ownerId) {
        this.fileName = new FileName(fileName);
        this.contentType = contentType;
        this.size = size;
        this.inputStream = inputStream;
        this.isTrashed = isTrashed;
        ancestorClosures.add(FileClosure.builder()
                .ancestor(this)
                .descendant(this)
                .depth(0L)
                .build());
        this.ownerId = ownerId;
    }

    public static File createDrive(UUID userId) {
        File file = new File();
        file.ownerId = userId;
        file.fileName = new FileName("drive");
        file.contentType = "drive";
        file.ancestorClosures.add(FileClosure.builder()
                .ancestor(file)
                .descendant(file)
                .depth(0L)
                .build());
        return file;
    }

    public boolean isFile() {
        return FileType.of(contentType) == FileType.FILE;
    }

    public boolean isFolder() {
        return FileType.of(contentType) == FileType.FOLDER;
    }

    public boolean isDrive() {
        return FileType.of(contentType) == FileType.DRIVE;
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

    /**
     * @return 파일이 존재하는 트리에서의 깊이
     */
    public Long getDepth() {
        return ancestorClosures.stream()
                .map(FileClosure::getDepth)
                .max(Long::compareTo)
                .orElse(0L);
    }

    public File getParent() {
        return ancestorClosures.stream()
                .filter(ancestorClosure -> ancestorClosure.getDepth() == 1)
                .map(FileClosure::getAncestor)
                .findFirst()
                .orElse(null);
    }

    public File getDrive() {
        return ancestorClosures.stream()
                .max(Comparator.comparing(FileClosure::getDepth))
                .orElseThrow(() -> new IllegalStateException("Drive not found"))
                .getAncestor();
    }

    public Long getParentId() {
        File parent = getParent();
        return parent == null ? null : parent.getId();
    }

    public List<FileClosure> getAncestorClosures() {
        return Collections.unmodifiableList(ancestorClosures);
    }

    public List<File> getDescendants() {
        return descendantClosures.stream()
                .map(FileClosure::getDescendant)
                .toList();
    }

    public List<File> getChildren() {
        return descendantClosures.stream()
                .filter(FileClosure::isImmediate)
                .map(FileClosure::getDescendant)
                .toList();
    }

    public void addAncestorClosure(FileClosure ancestorClosure) {
        ancestorClosures.add(ancestorClosure);
    }

    public void removeAncestorClosureIf(Predicate<FileClosure> filter) {
        ancestorClosures.removeIf(filter);
    }

    public void clearAncestorClosures() {
        ancestorClosures.clear();
    }

    public void moveTo(File newParent) {
        log.info("moveTo");
        newParent.validContainable(this);
        changeParent(newParent);
        syncPathOfDescendants();
    }

    private void changeParent(File newParent) {
        log.info("changeParent");
        ancestorClosures.removeIf(ancestorClosure -> ancestorClosure.getDepth() >= 1);
        newParent.getAncestorClosures().stream().map(ancestorClosure -> FileClosure.builder()
                .ancestor(ancestorClosure.getAncestor())
                .descendant(this)
                .depth(ancestorClosure.getDepth() + 1)
                .build()
        ).forEach(ancestorClosures::add);
    }

    private void syncPathOfDescendants() {
        log.info("syncPathOfDescendants");
        descendantClosures.stream()
                .map(FileClosure::getDescendant)
                .filter(descendant -> !descendant.equals(this))
                .forEach(descendant -> descendant.syncPathWith(this));
    }

    private void syncPathWith(File targetAncestor) {
        Long targetDepth = getAncestorClosures().stream()
                .filter(ancestorClosure -> ancestorClosure.ancestorIs(targetAncestor))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Target is not ancestor"))
                .getDepth();
        removeAncestorClosureIf(ancestorClosure -> ancestorClosure.getDepth() > targetDepth);
        targetAncestor.getAncestorClosures().stream()
                .filter(ancestorClosure -> ancestorClosure.getDepth() > 0)
                .map(ancestorClosure -> FileClosure.builder()
                        .ancestor(ancestorClosure.getAncestor())
                        .descendant(this)
                        .depth(ancestorClosure.getDepth() + targetDepth)
                        .build()
                ).forEach(this::addAncestorClosure);
    }

    /**
     * 특정 파일을 후손으로 연결할 수 있는지 검증합니다.
     *
     * @param file 후손으로 연결할 파일
     */
    public void validContainable(File file) {
        log.info("validContainable");
        if (!isOwner(file.ownerId)) {
            throw new ForbiddenException("new parent's owner is not match");
        }
        if (isTrashed()) {
            throw new BadRequestException("new parent is trashed");
        }

        if (isAncestorTrashed()) {
            throw new BadRequestException("new parent's ancestor is trashed");
        }

        if (cycleDetected(file)) {
            throw new BadRequestException("cycle detected");
        }

        if (isExist(file.getFileName())) {
            throw new BadRequestException("file name duplicated");
        }
    }

    /**
     * 조상 파일 중 하나라도 삭제 상태인지 확인합니다.
     *
     * @return
     */
    public boolean isAncestorTrashed() {
        log.info("isAncestorTrashed");
        return ancestorClosures.stream()
                .map(FileClosure::getAncestor)
                .anyMatch(File::isTrashed);
    }

    private boolean cycleDetected(File file) {
        return ancestorClosures.stream()
                .map(FileClosure::getAncestor)
                .anyMatch(file::equals);
    }

    public boolean isExist(FileName fileName) {
        return getChildren().stream()
                .map(File::getFileName)
                .anyMatch(fileName::equals);
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

    public boolean isOwner(UUID userId) {
        return ownerId.equals(userId);
    }
}
