package com.example.nyeondrive.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.nyeondrive.dto.service.CreateFileDto;
import com.example.nyeondrive.dto.service.UpdateFileDto;
import com.example.nyeondrive.entity.File;
import com.example.nyeondrive.repository.FileRepository;
import com.example.nyeondrive.vo.FileName;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FileServiceTest {
    FileRepository fileRepository = Mockito.mock(FileRepository.class);
    FileService fileService = new FileService(fileRepository);
    File root;
    File textFile;
    File folder;

    @BeforeEach
    void setUp() {
        root = File.createRootFolder();
        textFile = File.builder().fileName("text.txt").contentType("plain/text").size(100L).parent(root)
                .isTrashed(false).build();
        folder = File.builder().fileName("folder").contentType("folder").size(100L).parent(root).isTrashed(false)
                .build();
    }

    @Test
    @DisplayName("파일 생성_루트에 생성_성공")
    void createFile() {
        // given
        CreateFileDto createFileDto = new CreateFileDto("test", 1L, "folder", false);
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.of(root));
        // when
        File file = fileService.createFile(createFileDto);
        // then
        Mockito.verify(fileRepository).save(file);
    }

    @Test
    @DisplayName("파일 생성_폴더에 생성_성공")
    void createFileInFolder() {
        // given
        CreateFileDto createFileDto = new CreateFileDto("test", 1L, "plain/text", false);
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.of(folder));
        // when
        File file = fileService.createFile(createFileDto);
        // then
        Mockito.verify(fileRepository).save(file);
    }

    @Test
    @DisplayName("파일 생성_파일에 생성_실패")
    void createFile_fail() {
        // given
        CreateFileDto createFileDto = new CreateFileDto("test", 1L, "file", false);
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.of(textFile));
        // when & then
        assertThrows(RuntimeException.class, () -> fileService.createFile(createFileDto));
    }

    @Test
    @DisplayName("파일 찾기_성공")
    void findFile() {
        // given
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.of(textFile));
        // when
        File foundFile = fileService.findFile(1L);
        // then
        Mockito.verify(fileRepository).findById(1L);
        Assertions.assertThat(foundFile).isEqualTo(textFile);
    }

    @Test
    @DisplayName("파일 찾기_실패")
    void findFile_fail() {
        // given
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.empty());
        // when & then
        assertThrows(RuntimeException.class, () -> fileService.findFile(1L));
    }

    @Test
    @DisplayName("파일 수정_모든 필드_성공")
    void updateFile() {
        // given
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.of(textFile));
        Mockito.when(fileRepository.findById(2L)).thenReturn(Optional.of(folder));
        UpdateFileDto updateFileDto = new UpdateFileDto("newName.png", 2L, "image/png", true);
        // when
        File updatedFile = fileService.updateFile(1L, updateFileDto);
        // then
        Mockito.verify(fileRepository).save(updatedFile);
        Assertions.assertThat(updatedFile.getFileName()).isEqualTo(new FileName("newName.png"));
        Assertions.assertThat(updatedFile.getParent()).isEqualTo(folder);
        Assertions.assertThat(updatedFile.getContentType()).isEqualTo("image/png");
        Assertions.assertThat(updatedFile.isTrashed()).isTrue();
    }

    @Test
    @DisplayName("파일 수정_타겟 파일 존재x_실패")
    void updateFile_fail() {
        // given
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.empty());
        UpdateFileDto updateFileDto = new UpdateFileDto("newName.png", 2L, "image/png", true);
        // when & then
        assertThrows(RuntimeException.class, () -> fileService.updateFile(1L, updateFileDto));
    }

    @Test
    @DisplayName("파일 수정_부모 존재x_실패")
    void updateFile_fail2() {
        // given
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.of(textFile));
        Mockito.when(fileRepository.findById(2L)).thenReturn(Optional.empty());
        UpdateFileDto updateFileDto = new UpdateFileDto("newName.png", 2L, "image/png", true);
        // when & then
        assertThrows(RuntimeException.class, () -> fileService.updateFile(1L, updateFileDto));
    }

    @Test
    @DisplayName("피일 수정_부모가 파일인 경우_실패")
    void updateFile_fail3() {
        // given
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.of(folder));
        Mockito.when(fileRepository.findById(2L)).thenReturn(Optional.of(textFile));
        UpdateFileDto updateFileDto = new UpdateFileDto("newName.png", 2L, "image/png", true);
        // when & then
        assertThrows(RuntimeException.class, () -> fileService.updateFile(1L, updateFileDto));
    }

    @Test
    @DisplayName("파일 목록 조회_성공")
    void listFile() {
        // given
        // when
        fileService.listFile(null, null, null);
        // then
        Mockito.verify(fileRepository).findAll(null, null, null);
    }

    @Test
    @DisplayName("파일 삭제_성공")
    void deleteFile() {
        // given
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.of(textFile));
        // when
        fileService.deleteFile(1L);
        // then
        Mockito.verify(fileRepository).delete(textFile);
    }

    @Test
    @DisplayName("파일 삭제_파일 존재x_실패")
    void deleteFile_fail() {
        // given
        Mockito.when(fileRepository.findById(1L)).thenReturn(Optional.empty());
        // when & then
        assertThrows(RuntimeException.class, () -> fileService.deleteFile(1L));
    }
}