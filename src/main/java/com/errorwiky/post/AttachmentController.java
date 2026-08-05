package com.errorwiky.post;

import com.errorwiky.common.BusinessException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
    private final AttachmentRepository attachments; private final FileStorageService storage;
    public AttachmentController(AttachmentRepository attachments,FileStorageService storage){this.attachments=attachments;this.storage=storage;}
    @GetMapping("/{id}") ResponseEntity<Resource> download(@PathVariable(name = "id") Long id){
        AttachmentEntity file=attachments.findById(id).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"첨부파일을 찾을 수 없습니다."));
        String encoded=UriUtils.encode(file.getOriginalName(),StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType()==null?"application/octet-stream":file.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename*=UTF-8''"+encoded).body(storage.load(file.getStoredName()));
    }
}
