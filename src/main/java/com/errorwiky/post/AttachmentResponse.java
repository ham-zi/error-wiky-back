package com.errorwiky.post;
public record AttachmentResponse(Long attachmentId,String originalName,String contentType,long fileSize,String downloadUrl){
    public static AttachmentResponse from(AttachmentEntity a){
        return new AttachmentResponse(a.getId(),a.getOriginalName(),a.getContentType(),a.getFileSize(),"/api/attachments/"+a.getId());
    }
}
