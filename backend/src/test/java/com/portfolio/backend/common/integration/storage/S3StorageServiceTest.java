package com.portfolio.backend.common.integration.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3StorageService s3StorageService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3StorageService, "bucketName", bucketName);
        ReflectionTestUtils.setField(s3StorageService, "region", "ap-northeast-2");
    }

    @Test
    void putObject_withKey_shouldUploadToS3AndReturnUrl() throws IOException {
        // Given
        String currentDateDirectory = LocalDate.now().format(formatter);
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );
        String key = "custom-key.jpg";

        // When
        String result = s3StorageService.putObject(file, key);

        // Then
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertTrue(result.startsWith("https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + currentDateDirectory));
    }

    @Test
    void putObject_withDirectoryAndFilename_shouldGenerateKeyAndUploadToS3() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );
        String directory = "images";
        String filename = "custom-name.jpg";

        // When
        String result = s3StorageService.putObject(file, filename);

        // Then
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertTrue(result.startsWith("https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/"));
        assertTrue(result.endsWith(".jpg")); // UUID가 포함되어 있어 정확한 이름 확인은 불가
    }

    @Test
    void deleteObject_shouldDeleteFromS3() {
        // Given
        String fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test.jpg";

        // When
        boolean result = s3StorageService.deleteObject(fileUrl);

        // Then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
        assertTrue(result);
    }

    @Test
    void deleteObject_whenExceptionOccurs_shouldReturnFalse() {
        // Given
        String fileUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/images/test.jpg";
        doThrow(new RuntimeException("S3 Error")).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // When
        boolean result = s3StorageService.deleteObject(fileUrl);

        // Then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
        assertFalse(result);
    }
}
