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
        // 상위 파일 삭제 여부 확인
        File fileReference = fileRepository.getReferenceById(fileId);
        if (isAncestorTrashed(fileReference)) {
            throw new BadRequestException("Parent file is trashed");
        }
        // 파일 및 부모 아이디 찾기
        File file = findWithParent(fileId);
        return FileDto.of(file.getParent().getId(), file);
    }

    private boolean isAncestorTrashed(File file) {
        List<FileClosure> trashedAncestorClosures = fileClosureRepository.findAllByDescendantAndAncestor_isTrashed(
                file, true);
        return !trashedAncestorClosures.isEmpty();
    }

    private File findWithParent(Long fileId) {
        File file = fileRepository.findWithAncestorClosuresById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));
        cacheParent(file);
        return file;
    }

    /**
     * 파일의 부모 파일을 찾는다.
     * 캐싱된 부모 파일이 없으면 DB에서 조회하며 캐싱한다.
     * 부모 파일은 아이디만 사용되는 경우가 많아 프록시 객체로 반환한다.
     * @param file 파일
     * @return 부모 파일
     */
    private File findParent(File file) {
        // 캐싱된 부모 리턴
        if (file.getParent() != null) {
            return file.getParent();
        }

        // 부모 파일 조회
        Optional<FileClosure> parentClosure = fileClosureRepository.findWithAncestorByDescendantAndDepth(file, 1L);
        File parentReference = parentClosure
                .map(FileClosure::getAncestor)
                .orElse(null);
        file.setParent(parentReference);
        return file.getParent();
    }

    /**
     * 드라이브 생성
     * 이미 드라이브가 존재하면 예외 발생
     * 드라이브의 부모는 Null이다.
     * @param userId 사용자 아이디
     * @return 생성된 드라이브
     */
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

    /**
     * 파일 이동
     * 파일의 부모를 변경한다.
     * 파일의 부모가 변경되면 클로저를 갱신한다.
     * @param file 파일
     * @param newParentId 새 부모 파일 아이디
     */
    private void moveFile(File file, Long newParentId) {
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


    /**
     * 새로운 폴더로 옮겨 졌을 때 순환 구조가 발생하는지 확인
     * 새로운 폴더의 조상에 현재 파일이 이미 존재하는지 확인
     * @param file
     * @param parent
     * @return
     */
    private boolean detectCycle(File file, File parent) {
        return parent.getAncestorClosures()
                .stream()
                .anyMatch(ancestorClosure -> ancestorClosure.getAncestor().equals(file));
    }


    /**
     * 파일의 서브트리를 전체 트리에서 분리한다.
     * <서브트리의 조상 - 서브트리에 포함된 모든 노드> 관계의 클로저 삭제
     * @param file
     */
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

    /**
     * 파일의 서브트리를 새로운 부모에 연결한다.
     * <새로운 부모의 조상 - 서브트리에 포함된 모든 노드> 관계의 클로저 생성
     * @param file
     * @param parent
     */
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
