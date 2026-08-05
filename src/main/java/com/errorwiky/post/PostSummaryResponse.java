package com.errorwiky.post;

import java.time.LocalDateTime;

public record PostSummaryResponse(Long postId, BoardType boardType, String title, ErrorCategory category,
                                  Long authorId, String authorName, long viewCount, long likeCount,
                                  long commentCount, LocalDateTime createdAt) {
    public static PostSummaryResponse from(PostEntity p) {
        return new PostSummaryResponse(p.getId(),p.getBoardType(),p.getTitle(),p.getCategory(),
                p.getAuthor().getId(),p.getAuthor().getName(),p.getViewCount(),p.getLikeCount(),
                p.getCommentCount(),p.getCreatedAt());
    }
}
