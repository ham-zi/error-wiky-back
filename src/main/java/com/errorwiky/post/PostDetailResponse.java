package com.errorwiky.post;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(Long postId, BoardType boardType, String title, String content,
 ErrorCategory category, String errorMessage, String environment, String cause, String solution,
 Long authorId, String authorName, long viewCount, long likeCount, long commentCount,
 boolean likedByMe, boolean editable, LocalDateTime createdAt, LocalDateTime updatedAt,
 List<AttachmentResponse> attachments) {}
