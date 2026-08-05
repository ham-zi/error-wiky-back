package com.errorwiky.post;

import com.errorwiky.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EW_POSTS", indexes = {
        @Index(name = "IDX_EW_POST_BOARD_CREATED", columnList = "board_type, created_at"),
        @Index(name = "IDX_EW_POST_AUTHOR", columnList = "author_id")
})
public class PostEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(name="board_type", nullable=false, length=20)
    private BoardType boardType;
    @Column(nullable=false, length=200)
    private String title;
    @Lob @Column(nullable=false)
    private String content;
    @Enumerated(EnumType.STRING) @Column(length=30)
    private ErrorCategory category;
    @Lob @Column(name="error_message") private String errorMessage;
    @Lob private String environment;
    @Lob private String cause;
    @Lob private String solution;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="author_id", nullable=false)
    private UserEntity author;
    @Column(name="view_count", nullable=false) private long viewCount;
    @Column(name="like_count", nullable=false) private long likeCount;
    @Column(name="comment_count", nullable=false) private long commentCount;
    @Column(nullable=false) private boolean deleted;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;

    protected PostEntity() {}
    public static PostEntity create(PostRequest request, UserEntity author) {
        PostEntity p = new PostEntity(); p.apply(request); p.author = author; return p;
    }
    public void apply(PostRequest r) {
        this.boardType=r.boardType(); this.title=r.title().trim(); this.content=r.content().trim();
        if (r.boardType()==BoardType.ERROR_WIKI) {
            this.category=r.category(); this.errorMessage=clean(r.errorMessage()); this.environment=clean(r.environment());
            this.cause=clean(r.cause()); this.solution=clean(r.solution());
        } else { this.category=null; this.errorMessage=null; this.environment=null; this.cause=null; this.solution=null; }
    }
    private static String clean(String v) { return v==null || v.isBlank() ? null : v.trim(); }
    @PrePersist void prePersist(){ createdAt=updatedAt=LocalDateTime.now(); }
    @PreUpdate void preUpdate(){ updatedAt=LocalDateTime.now(); }
    public void increaseViews(){ viewCount++; }
    public void increaseLikes(){ likeCount++; }
    public void decreaseLikes(){ likeCount=Math.max(0, likeCount-1); }
    public void increaseComments(){ commentCount++; }
    public void decreaseComments(){ commentCount=Math.max(0, commentCount-1); }
    public void delete(){ deleted=true; }
    public Long getId(){return id;} public BoardType getBoardType(){return boardType;} public String getTitle(){return title;}
    public String getContent(){return content;} public ErrorCategory getCategory(){return category;} public String getErrorMessage(){return errorMessage;}
    public String getEnvironment(){return environment;} public String getCause(){return cause;} public String getSolution(){return solution;}
    public UserEntity getAuthor(){return author;} public long getViewCount(){return viewCount;}
    public long getLikeCount(){return likeCount;} public long getCommentCount(){return commentCount;} public boolean isDeleted(){return deleted;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
