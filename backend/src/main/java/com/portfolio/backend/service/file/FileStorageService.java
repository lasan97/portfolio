package com.portfolio.backend.service.file;

import com.portfolio.backend.common.integration.aws.S3Helper;
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
public class FileStorageService {

    private final S3Helper s3Helper;

    public String uploadFile(MultipartFile file) throws IOException {
        return s3Helper.putObject(file, null);
    }

    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            fileUrls.add(uploadFile(file));
        }
        
        return fileUrls;
    }

    public boolean deleteFile(String fileUrl) {
        return s3Helper.deleteObject(fileUrl);
    }
}

