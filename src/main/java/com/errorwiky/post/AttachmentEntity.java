package com.errorwiky.post;

import jakarta.persistence.*;

@Entity
@Table(name = "EW_ATTACHMENTS")
public class AttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    // 사용자가 업로드한 원래 파일명
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    // S3 객체 Key
    // 예: posts/550e8400-e29b-41d4-a716-446655440000_image.png
    @Column(name = "stored_name", nullable = false, unique = true, length = 500)
    private String storedName;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    protected AttachmentEntity() {
    }

    public AttachmentEntity(
            PostEntity post,
            String originalName,
            String storedName,
            String contentType,
            long fileSize
    ) {
        this.post = post;
        this.originalName = originalName;
        this.storedName = storedName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public Long getId() {
        return id;
    }

    public PostEntity getPost() {
        return post;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }
}