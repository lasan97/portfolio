package com.portfolio.backend.common.integration.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Helper {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String putObject(MultipartFile file, String directory, String filename) throws IOException {
        String fileKey = generateKeyWithDirectory(directory, filename != null ? filename : file.getOriginalFilename());
        return putObject(file, fileKey);
    }

    public String putObject(MultipartFile file, String key) throws IOException {
        String fileKey = key != null ? key : generateKey(file.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return generateFileUrl(fileKey);
    }

    public boolean deleteObject(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(request);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", fileUrl, e);
            return false;
        }
    }

    private String generateKey(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        return UUID.randomUUID() + "." + extension;
    }

    private String generateKeyWithDirectory(String directory, String filename) {
        if (StringUtils.hasText(directory)) {
            if (directory.endsWith("/")) {
                return directory + generateKey(filename);
            } else {
                return directory + "/" + generateKey(filename);
            }
        }
        return generateKey(filename);
    }

    private String generateFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    private String extractKeyFromUrl(String fileUrl) {
        // URL에서 키 추출 (https://bucket-name.s3.region.amazonaws.com/key 형식 가정)
        return fileUrl.substring(fileUrl.indexOf(".com/") + 5);
    }
}
