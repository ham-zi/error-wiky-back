package com.errorwiky.post;

import com.errorwiky.common.ApiResponse;
import com.errorwiky.common.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>> list(
            @RequestParam(name = "boardType") BoardType boardType,
            @RequestParam(name = "category", required = false)
            ErrorCategory category,
            @RequestParam(name = "keyword", required = false)
            String keyword,
            @RequestParam(name = "mine", defaultValue = "false")
            boolean mine,
            @RequestParam(name = "page", defaultValue = "0")
            int page,
            @RequestParam(name = "size", defaultValue = "10")
            int size,
            Authentication authentication
    ) {
        return ApiResponse.ok(
                service.list(
                        boardType,
                        category,
                        keyword,
                        mine,
                        page,
                        size,
                        authentication
                )
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> detail(
            @PathVariable(name = "id") Long id,
            Authentication authentication
    ) {
        return ApiResponse.ok(service.detail(id, authentication));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDetailResponse>> create(
            @Valid
            @RequestPart(name = "data") PostRequest request,
            @RequestPart(name = "files", required = false)
            List<MultipartFile> files,
            Authentication authentication
    ) {
        return ResponseEntity.status(201)
                .body(ApiResponse.ok(
                        service.create(request, files, authentication)
                ));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<PostDetailResponse> update(
            @PathVariable(name = "id") Long id,
            @Valid
            @RequestPart(name = "data") PostRequest request,
            @RequestPart(name = "files", required = false)
            List<MultipartFile> files,
            Authentication authentication
    ) {
        return ApiResponse.ok(
                service.update(id, request, files, authentication)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable(name = "id") Long id,
            Authentication authentication
    ) {
        service.delete(id, authentication);
        return ApiResponse.ok("게시글을 삭제했습니다.");
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> like(
            @PathVariable(name = "id") Long id,
            Authentication authentication
    ) {
        return ApiResponse.ok(
                service.toggleLike(id, authentication)
        );
    }
}