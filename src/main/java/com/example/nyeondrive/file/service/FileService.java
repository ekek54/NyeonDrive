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
import com.example.nyeondrive.file.repository.FileRepository;
import com.example.nyeondrive.file.vo.FileName;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /**
     * 파일 생성 조상 파일과 클로저를 생성한다.
     *
     * @param createFileDto: 파일 생성 정보
     * @return: 생성된 파일
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

        file.moveTo(parent);
        fileRepository.save(file);
        return FileDto.of(file);
    }

    /**
     * 파일 조회 파일이 삭제 상태인지 확인 상위 폴더가 삭제 상태인지 확인
     *
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
        loadAncestors(file);
        if (file.isAncestorTrashed()) {
            throw new BadRequestException("Parent file is trashed");
        }

        // 파일 및 부모 아이디 찾기
        return FileDto.of(file);
    }

    private void loadAncestors(File file) {
        log.info("load ancestors");
        List<File> ancestors = fileRepository.findAllByDescendant(file);
    }

    /**
     * 드라이브 생성 이미 드라이브가 존재하면 예외 발생 드라이브의 부모는 Null이다.
     *
     * @param userId 사용자 아이디
     * @return 생성된 드라이브
     */
    public FileDto createDrive(UUID userId) {
        if (hasDrive(userId)) {
            throw new BadRequestException("Drive already exists");
        }
        File drive = File.createDrive(userId);
        fileRepository.save(drive);
        return FileDto.of(drive);
    }

    private boolean hasDrive(UUID userId) {
        return fileRepository.findByOwnerIdAndContentType(userId, FileType.DRIVE_CONTENT_TYPE).isPresent();
    }

    /**
     * 파일 수정 파일 이름, 부모, 컨텐츠 타입, 삭제 상태를 변경한다.
     *
     * @param fileId:        수정할 파일 아이디
     * @param updateFileDto: 수정할 파일 정보
     * @return 수정된 파일
     */
    public FileDto updateFile(Long fileId, UpdateFileDto updateFileDto) {
        log.info("update file");
        File file = fileRepository.findWithAncestorClosuresById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));

        if (updateFileDto.isTrashed() != null) {
            if (updateFileDto.isTrashed()) {
                file.trash();
            } else {
                if(file.isAncestorTrashed()) {
                    moveFile(file, file.getDrive().getId());
                }
                file.restore();
            }
        }

        if (updateFileDto.name() != null) {
            file.setFileName(new FileName(updateFileDto.name()));
        }

        if (updateFileDto.parentId() != null) {
            moveFile(file, updateFileDto.parentId());
        }

        if (updateFileDto.contentType() != null) {
            file.setContentType(updateFileDto.contentType());
        }
        fileRepository.save(file);
        return FileDto.of(file);
    }

    /**
     * 파일 이동 파일의 부모를 변경한다. 파일의 부모가 변경되면 클로저를 갱신한다.
     *
     * @param file        파일
     * @param newParentId 새 부모 파일 아이디
     */
    private void moveFile(File file, Long newParentId) {
        log.info("move file");
        log.info("find new parent");
        File newParent = fileRepository.findWithAncestorClosuresById(newParentId)
                .orElseThrow(() -> new BadRequestException("Parent not found"));
        loadDescendantsWithAncestorClosures(file);
        loadDescendants(newParent);
        file.moveTo(newParent);
    }

    private List<File> loadDescendantsWithAncestorClosures(File file) {
        log.info("load descendants with ancestor closures");
        List<Long> descendantIds = file.getDescendants().stream().map(File::getId).toList();
        return fileRepository.findAllWithAncestorClosuresByIdIn(descendantIds);
    }

    private List<File> loadDescendants(File file) {
        log.info("load descendants");
        List<Long> descendantIds = file.getDescendants().stream().map(File::getId).toList();
        return fileRepository.findAllByIdIn(descendantIds);
    }

    public List<FileDto> listFile(FileFilterDto fileFilterDto, FilePagingDto filePagingDto,
                                  List<FileOrderDto> fileOrderDtos) {
        return fileRepository.findAll(fileFilterDto, filePagingDto, fileOrderDtos).stream()
                .map(FileDto::of)
                .toList();
    }

    public void deleteFile(Long fileId) {
        File file = fileRepository.findWithAncestorClosuresById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (file.isDrive()) {
            throw new BadRequestException("Drive cannot be deleted");
        }
        List<File> descendants = loadDescendantsWithAncestorClosures(file);
        descendants.forEach(File::clearAncestorClosures);
        fileRepository.flush();
        fileRepository.deleteAllInBatch(descendants);
    }
}
