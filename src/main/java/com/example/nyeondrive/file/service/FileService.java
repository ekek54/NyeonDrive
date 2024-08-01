package com.example.nyeondrive.file.service;

import com.example.nyeondrive.exception.error.BadRequestException;
import com.example.nyeondrive.exception.error.NotFoundException;
import com.example.nyeondrive.file.constant.FileType;
import com.example.nyeondrive.file.dto.service.CreateFileDto;
import com.example.nyeondrive.file.dto.service.FileDto;
import com.example.nyeondrive.file.dto.service.FileFilterDto;
import com.example.nyeondrive.file.dto.service.FileOrderDto;
import com.example.nyeondrive.file.dto.service.FilePagingDto;
import com.example.nyeondrive.file.dto.service.UpdateFileDto;
import com.example.nyeondrive.file.entity.File;
import com.example.nyeondrive.file.entity.FileClosure;
import com.example.nyeondrive.file.repository.FileClosureRepository;
import com.example.nyeondrive.file.repository.FileRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
public class FileService {
    private final FileRepository fileRepository;
    private final FileClosureRepository fileClosureRepository;

    public FileService(FileRepository fileRepository, FileClosureRepository fileClosureRepository) {
        this.fileRepository = fileRepository;
        this.fileClosureRepository = fileClosureRepository;
    }

    public FileDto createFile(CreateFileDto createFileDto) {
        // 조상이 될 파일 목록을 얻기 위해 부모 파일의 조상과 연결된 클로저 탐색.
        File parentReference = fileRepository.getReferenceById(createFileDto.parentId());
        List<FileClosure> ancestorClosures = fileClosureRepository.findAllByDescendant(parentReference);
        if (ancestorClosures.isEmpty()) {
            throw new BadRequestException("Parent not found");
        }

        // 파일 생성
        File file = File.builder()
                .fileName(createFileDto.name())
                .contentType(createFileDto.contentType())
                .isTrashed(createFileDto.isTrashed())
                .build();
        fileRepository.save(file);

        // 조상 파일 - 새 파일 클로저 생성
        List<FileClosure> newClosures = new ArrayList<>();
        for (FileClosure ancestorClosure : ancestorClosures) {
            FileClosure newClosure = FileClosure.builder()
                    .ancestor(ancestorClosure.getAncestor())
                    .descendant(file)
                    .depth(ancestorClosure.getDepth() + 1)
                    .build();
            newClosures.add(newClosure);
        }

        // 새 파일 - 새 파일 클로저 생성
        FileClosure selfClosure = FileClosure.builder()
                .ancestor(file)
                .descendant(file)
                .depth(0L)
                .build();
        newClosures.add(selfClosure);
        fileClosureRepository.saveAll(newClosures);
        return FileDto.of(createFileDto.parentId(), file);
    }

    public FileDto findFile(Long fileId) {
        //TODO: 조상이 삭제 처리 되어 있다면 에러 처리
        File file = fileRepository.findById(fileId).
                orElseThrow(() -> new NotFoundException("File not found"));
        Optional<FileClosure> parentClosure = fileClosureRepository
                .findWithAncestorByDescendantAndDepth(file, 1L);
        Long parentId = findParent(file) == null ? null : findParent(file).getId();
        return FileDto.of(parentId, file);

    }

    private File findParent(File file) {
        if (file.getParent() != null) {
            return file.getParent();
        }
        Optional<FileClosure> parentClosure = fileClosureRepository.findWithAncestorByDescendantAndDepth(file, 1L);
        File parentReference = parentClosure
                .map(FileClosure::getAncestor)
                .orElse(null);
        file.setParent(parentReference);
        return file.getParent();
    }

    public FileDto createDrive(UUID userId) {
        fileRepository.findByOwnerIdAndContentType(userId, FileType.DRIVE_CONTENT_TYPE)
                .ifPresent(file -> {
                    throw new BadRequestException("Drive already exists");
                });
        File drive = File.createDrive(userId);
        fileRepository.save(drive);
        FileClosure selfClosure = FileClosure.builder()
                .ancestor(drive)
                .descendant(drive)
                .depth(0L)
                .build();
        fileClosureRepository.save(selfClosure);
        return FileDto.of(null, drive);
    }

    public FileDto updateFile(Long fileId, UpdateFileDto updateFileDto) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (updateFileDto.name() != null) {
            file.setFileName(updateFileDto.name());
        }
        if (updateFileDto.parentId() != null) {
            moveFile(file, updateFileDto.parentId());
        }
        if (updateFileDto.contentType() != null) {
            file.setContentType(updateFileDto.contentType());
        }
        if (updateFileDto.isTrashed() != null) {
            file.setTrashed(updateFileDto.isTrashed());
        }
        fileRepository.save(file);
        Long parentId = findParent(file).getId();
        return FileDto.of(parentId, file);
    }

    private void moveFile(File file, Long newParentId) {
        System.out.println("moveFile: " + file.getId() + " -> " + newParentId);
        File newParent = fileRepository.findWithAncestorClosuresById(newParentId)
                .orElseThrow(() -> new BadRequestException("Parent not found"));
        validateParent(file, newParent);
        detachSubtree(file);
        attachSubtree(file, newParent);
    }

    private void validateParent(File file, File parent) {
        if (parent.isFile()) {
            throw new BadRequestException("Parent is not a folder");
        }

        if (detectCycle(file, parent)) {
            throw new BadRequestException("cycle detected");
        }
    }

    private boolean detectCycle(File file, File parent) {
        return parent.getAncestorClosures()
                .stream()
                .anyMatch(ancestorClosure -> ancestorClosure.getAncestor().equals(file));
    }

    private void detachSubtree(File file) {
        loadAllClosures(file);
        List<File> nodesInSubtree = file.getDescendantClosures()
                .stream()
                .map(FileClosure::getDescendant)
                .toList();
        List<File> ancestorsOfSubtreeRoot = file.getAncestorClosures()
                .stream()
                .filter(FileClosure::isNotSelf)
                .map(FileClosure::getAncestor)
                .toList();
        fileClosureRepository.deleteAllInBatchByAncestorInAndDescendantIn(ancestorsOfSubtreeRoot, nodesInSubtree);
    }

    private void attachSubtree(File file, File parent) {
        log.trace("attachSubtree: {}", file.getId());
        List<FileClosure> newClosures = new ArrayList<>();
        for (FileClosure ancestorClosure : parent.getAncestorClosures()) {
            for (FileClosure descendantClosure : file.getDescendantClosures()) {
                FileClosure newClosure = FileClosure.builder()
                        .ancestor(ancestorClosure.getAncestor())
                        .descendant(descendantClosure.getDescendant())
                        .depth(ancestorClosure.getDepth() + descendantClosure.getDepth() + 1)
                        .build();
                newClosures.add(newClosure);
            }
        }
        fileClosureRepository.saveAll(newClosures);
    }

    private void loadDescendantClosures(File file) {
        fileRepository.findWithDescendantClosuresById(file.getId())
                .orElseThrow(() -> new NotFoundException("File not found"));
    }

    private void loadAncestorClosures(File file) {
        fileRepository.findWithAncestorClosuresById(file.getId())
                .orElseThrow(() -> new NotFoundException("File not found"));
        cacheParent(file);
    }

    private void loadAllClosures(File file) {
        fileRepository.findWithAncestorClosuresAndDescendantClosuresById(file.getId())
                .orElseThrow(() -> new NotFoundException("File not found"));
        cacheParent(file);
    }

    private void cacheParent(File file) {
        FileClosure parentClosure = file.getAncestorClosures().stream()
                .filter(FileClosure::isImmediate)
                .findFirst()
                .orElse(null);
        file.setParent(parentClosure == null ? null : parentClosure.getAncestor());
    }

    public List<FileDto> listFile(FileFilterDto fileFilterDto, FilePagingDto filePagingDto,
                                  List<FileOrderDto> fileOrderDtos) {
        return fileClosureRepository.findAll(fileFilterDto, filePagingDto, fileOrderDtos).stream()
                .map(fileClosure -> FileDto.of(fileClosure.getAncestor().getId(), fileClosure.getDescendant()))
                .toList();
    }
}
