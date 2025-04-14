package com.portfolio.backend.controller.file;

import com.portfolio.backend.controller.file.dto.FileUploadResponse;
import com.portfolio.backend.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String fileUrl = fileService.uploadFile(file);
            
            FileUploadResponse response = FileUploadResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload-multiple")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files
    ) {
        try {
            List<String> fileUrls = fileService.uploadFiles(files);
            
            List<FileUploadResponse> responses = files.stream()
                    .map(file -> {
                        int index = files.indexOf(file);
                        return FileUploadResponse.builder()
                                .fileName(file.getOriginalFilename())
                                .fileUrl(fileUrls.get(index))
                                .contentType(file.getContentType())
                                .size(file.getSize())
                                .build();
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            log.error("Failed to upload multiple files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        boolean deleted = fileService.deleteFile(fileUrl);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
