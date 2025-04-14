package com.portfolio.backend.service.file;

import com.portfolio.backend.common.integration.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final S3StorageService s3StorageService;

    public String uploadFile(MultipartFile file) throws IOException {
        return s3StorageService.putObject(file, null);
    }

    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            fileUrls.add(uploadFile(file));
        }
        
        return fileUrls;
    }

    public boolean deleteFile(String fileUrl) {
        return s3StorageService.deleteObject(fileUrl);
    }
}

