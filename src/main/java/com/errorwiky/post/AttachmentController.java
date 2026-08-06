package com.errorwiky.post;

import com.errorwiky.common.BusinessException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    public AttachmentController(
            AttachmentRepository attachmentRepository,
            FileStorageService fileStorageService
    ) {
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(
            @PathVariable(name = "id") Long id
    ) {
        AttachmentEntity attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "첨부파일을 찾을 수 없습니다."
                ));

        String encodedFileName = UriUtils.encode(
                attachment.getOriginalName(),
                StandardCharsets.UTF_8
        );

        MediaType mediaType = resolveMediaType(
                attachment.getContentType()
        );

        Resource resource = fileStorageService.load(
                attachment.getStoredName()
        );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName
                )
                .contentLength(attachment.getFileSize())
                .body(resource);
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}