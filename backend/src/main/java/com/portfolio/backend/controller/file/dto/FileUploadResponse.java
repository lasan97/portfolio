package com.portfolio.backend.controller.file.dto;

import lombok.Builder;

@Builder
public record FileUploadResponse(
    String fileName,
    String fileUrl,
    String contentType,
    Long size
) {
}
