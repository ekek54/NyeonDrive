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

    /**
     * 파일 생성
     * 조상 파일과 클로저를 생성한다.
     * TODO: 부모 폴더 내에 같은 이름의 파일이 있는지 확인
     * @param createFileDto: 파일 생성 정보
     * @return:  생성된 파일
     */
    public FileDto createFile(CreateFileDto createFileDto) {
        // 조상이 될 파일 목록을 얻기 위해 부모 파일의 조상과 연결된 클로저 탐색.
        File parent = fileRepository.findWithAncestorClosuresById(createFileDto.parentId())
                .orElseThrow(() -> new NotFoundException("Parent not found"));

        // 파일 생성
        File file = File.builder()
                .fileName(createFileDto.name())
                .contentType(createFileDto.contentType())
                .isTrashed(createFileDto.isTrashed())
                .build();

        // 조상 파일 - 새 파일 클로저 생성
        for (FileClosure ancestorClosure : parent.getAncestorClosures()) {
            FileClosure newClosure = FileClosure.builder()
                    .ancestor(ancestorClosure.getAncestor())
                    .descendant(file)
                    .depth(ancestorClosure.getDepth() + 1)
                    .build();
            parent.addDescendantClosure(newClosure);
            file.addAncestorClosure(newClosure);
        }

        // 새 파일 - 새 파일 클로저 생성
        FileClosure selfClosure = FileClosure.builder()
                .ancestor(file)
                .descendant(file)
                .depth(0L)
                .build();
        file.addDescendantClosure(selfClosure);
        file.addAncestorClosure(selfClosure);
        fileRepository.save(file);
        return FileDto.of(createFileDto.parentId(), file);
    }

    /**
     * 파일 조회
     * 파일이 삭제 상태인지 확인
     * 상위 폴더가 삭제 상태인지 확인
     * @param fileId: 조회 할 파일 아이디
     * @return 조회된 파일
     */
    public FileDto findFile(Long fileId) {
        // 파일 삭제 여부 확인
        File file = fileRepository.findWithAncestorClosuresById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (file.isTrashed()) {
            throw new NotFoundException("File is trashed");
        }

        // 상위 파일 삭제 여부 확인
        List<Long> ancestorIds = file.getAncestors().stream().map(File::getId).toList();
        fileRepository.findAllById(ancestorIds)
                .forEach(ancestor -> {
                    if (ancestor.isTrashed()) {
                        throw new BadRequestException("Ancestor file is trashed");
                    }
                });
        if (file.isAncestorTrashed()) {
            throw new BadRequestException("Parent file is trashed");
        }

        // 파일 및 부모 아이디 찾기
        return FileDto.of(file.getParent().getId(), file);
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
        FileClosure selfClosure = FileClosure.builder()
                .ancestor(drive)
                .descendant(drive)
                .depth(0L)
                .build();
        drive.addDescendantClosure(selfClosure);
        drive.addAncestorClosure(selfClosure);
        fileRepository.save(drive);
        return FileDto.of(null, drive);
    }


    /**
     * 파일 수정
     * 파일 이름, 부모, 컨텐츠 타입, 삭제 상태를 변경한다.
     * 삭제 상태를 해제해야 수정할 수 있다.
     * @param fileId: 수정할 파일 아이디
     * @param updateFileDto: 수정할 파일 정보
     * @return 수정된 파일
     */
    public FileDto updateFile(Long fileId, UpdateFileDto updateFileDto) {
        File file = fileRepository.findWithAncestorClosuresById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (updateFileDto.isTrashed() != null) {
            if (updateFileDto.isTrashed()) {
                file.trash();
            } else {
                file.restore();
            };
        }
        if (file.isTrashed()) {
            throw new BadRequestException("File is trashed");
        }
        if (file.isAncestorTrashed()) {
            throw new BadRequestException("Parent file is trashed");
        }
        if (updateFileDto.name() != null) {
            file.setFileName(updateFileDto.name());
        }
        if (updateFileDto.parentId() != null) {
            moveFile(file, updateFileDto.parentId());
        }
        if (updateFileDto.contentType() != null) {
            file.setContentType(updateFileDto.contentType());
        }
        fileRepository.save(file);
        return FileDto.of(file.getParent().getId(), file);
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


    /**
     * 이동할 위치가 유효한지 확인
     * 부모가 폴더인지 확인
     * 순환 구조가 발생하는지 확인
     * 부모 폴더가 삭제 상태인지 확인
     * TODO: 부모 폴더내에 같은 이름의 파일이 있는지 확인
     * @param file: 이동할 파일
     * @param parent: 새로운 부모 파일
     */
    private void validateParent(File file, File parent) {
        if (parent.isFile()) {
            throw new BadRequestException("Parent is not a folder");
        }
        if (parent.isTrashed()) {
            throw new BadRequestException("Parent is trashed");
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
        fileRepository.findWithDescendantClosuresById(file.getId())
                .orElseThrow(() -> new NotFoundException("File not found"));
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

    public List<FileDto> listFile(FileFilterDto fileFilterDto, FilePagingDto filePagingDto,
                                  List<FileOrderDto> fileOrderDtos) {
        return fileClosureRepository.findAll(fileFilterDto, filePagingDto, fileOrderDtos).stream()
                .map(fileClosure -> FileDto.of(fileClosure.getAncestor().getId(), fileClosure.getDescendant()))
                .toList();
    }
}
