package com.errorwiky.comment;
import java.time.LocalDateTime;
public record CommentResponse(Long commentId,Long authorId,String authorName,String content,boolean deleted,boolean editable,LocalDateTime createdAt){
 public static CommentResponse from(CommentEntity c,Long current){return new CommentResponse(c.getId(),c.getAuthor().getId(),c.getAuthor().getName(),c.getContent(),c.isDeleted(),current!=null&&current.equals(c.getAuthor().getId())&&!c.isDeleted(),c.getCreatedAt());}
}
