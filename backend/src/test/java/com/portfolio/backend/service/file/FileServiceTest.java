package com.portfolio.backend.service.file;

import com.portfolio.backend.common.integration.storage.S3StorageService;
import com.portfolio.backend.service.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("FileService 테스트")
class FileServiceTest extends ServiceTest {

    @MockBean
    private S3StorageService s3StorageService;

    @Autowired
    private FileService fileService;

    @Test
    @DisplayName("파일 업로드 시 S3 스토리지 서비스를 호출하고 파일 URL을 반환한다")
    void uploadFile_shouldCallS3StorageServiceAndReturnFileUrl() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        String expectedUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test.jpg";
        when(s3StorageService.putObject(eq(file), eq(null))).thenReturn(expectedUrl);

        // When
        String result = fileService.uploadFile(file);

        // Then
        verify(s3StorageService, times(1)).putObject(eq(file), eq(null));
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("여러 파일 업로드 시 각 파일마다 S3 스토리지 서비스를 호출한다")
    void uploadFiles_shouldCallS3StorageServiceForEachFile() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "file1", 
                "test1.jpg", 
                "image/jpeg", 
                "test image content 1".getBytes()
        );
        
        MockMultipartFile file2 = new MockMultipartFile(
                "file2", 
                "test2.jpg", 
                "image/jpeg", 
                "test image content 2".getBytes()
        );
        
        List<MultipartFile> files = Arrays.asList(file1, file2);
        
        when(s3StorageService.putObject(eq(file1), eq(null))).thenReturn("url1");
        when(s3StorageService.putObject(eq(file2), eq(null))).thenReturn("url2");

        // When
        List<String> results = fileService.uploadFiles(files);

        // Then
        verify(s3StorageService, times(1)).putObject(eq(file1), eq(null));
        verify(s3StorageService, times(1)).putObject(eq(file2), eq(null));
        assertEquals(2, results.size());
        assertEquals("url1", results.get(0));
        assertEquals("url2", results.get(1));
    }

    @Test
    @DisplayName("파일 삭제 시 S3 스토리지 서비스에 위임한다")
    void deleteFile_shouldDelegateToS3StorageService() {
        // Given
        String fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test.jpg";
        when(s3StorageService.deleteObject(fileUrl)).thenReturn(true);

        // When
        boolean result = fileService.deleteFile(fileUrl);

        // Then
        verify(s3StorageService, times(1)).deleteObject(fileUrl);
        assertTrue(result);
    }
}
