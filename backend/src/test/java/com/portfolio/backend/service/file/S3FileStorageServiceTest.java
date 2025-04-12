package com.portfolio.backend.service.file;

import com.portfolio.backend.common.integration.aws.S3Helper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

    @Mock
    private S3Helper s3Helper;

    @InjectMocks
    private FileStorageService fileStorageService;

    @Test
    void uploadFile_shouldCallS3HelperAndReturnFileUrl() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );
        String directory = "images";
        String expectedUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test-uuid.jpg";
        
        when(s3Helper.putObject(eq(file), eq(directory), eq(null))).thenReturn(expectedUrl);

        // When
        String result = fileStorageService.uploadFile(file, directory);

        // Then
        verify(s3Helper, times(1)).putObject(eq(file), eq(directory), eq(null));
        assertEquals(expectedUrl, result);
    }

    @Test
    void uploadFiles_shouldCallS3HelperForEachFile() throws IOException {
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
        String directory = "images";
        
        when(s3Helper.putObject(eq(file1), eq(directory), eq(null))).thenReturn("url1");
        when(s3Helper.putObject(eq(file2), eq(directory), eq(null))).thenReturn("url2");

        // When
        List<String> results = fileStorageService.uploadFiles(files, directory);

        // Then
        verify(s3Helper, times(1)).putObject(eq(file1), eq(directory), eq(null));
        verify(s3Helper, times(1)).putObject(eq(file2), eq(directory), eq(null));
        assertEquals(2, results.size());
        assertEquals("url1", results.get(0));
        assertEquals("url2", results.get(1));
    }

    @Test
    void deleteFile_shouldDelegateToS3Helper() {
        // Given
        String fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test.jpg";
        when(s3Helper.deleteObject(fileUrl)).thenReturn(true);

        // When
        boolean result = fileStorageService.deleteFile(fileUrl);

        // Then
        verify(s3Helper, times(1)).deleteObject(fileUrl);
        assertTrue(result);
    }
}
