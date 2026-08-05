package com.errorwiky.comment;
import com.errorwiky.post.PostEntity; import com.errorwiky.user.UserEntity; import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="EW_COMMENTS",indexes=@Index(name="IDX_EW_COMMENT_POST",columnList="post_id, created_at"))
public class CommentEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="post_id") private PostEntity post;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="author_id") private UserEntity author;
 @Column(nullable=false,length=2000) private String content; @Column(nullable=false) private boolean deleted;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 protected CommentEntity(){} public CommentEntity(PostEntity p,UserEntity a,String c){post=p;author=a;content=c.trim();}
 @PrePersist void pre(){createdAt=LocalDateTime.now();} public void delete(){deleted=true;content="삭제된 댓글입니다.";}
 public Long getId(){return id;} public PostEntity getPost(){return post;} public UserEntity getAuthor(){return author;} public String getContent(){return content;}
 public boolean isDeleted(){return deleted;} public LocalDateTime getCreatedAt(){return createdAt;}
}
