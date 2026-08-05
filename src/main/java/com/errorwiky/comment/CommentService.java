package com.errorwiky.comment;
import com.errorwiky.auth.CurrentUserService; import com.errorwiky.common.BusinessException; import com.errorwiky.post.*; import com.errorwiky.user.UserEntity;
import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.security.core.Authentication; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service
public class CommentService{
 private final CommentRepository comments; private final PostRepository posts; private final CurrentUserService current;
 public CommentService(CommentRepository c,PostRepository p,CurrentUserService u){comments=c;posts=p;current=u;}
 @Transactional(readOnly=true) public List<CommentResponse> list(Long postId,Authentication auth){
   UserEntity optional=current.optional(auth); Long uid=optional==null?null:optional.getId(); return comments.findByPostIdOrderByCreatedAtAsc(postId).stream().map(c->CommentResponse.from(c,uid)).toList(); }
 @Transactional public CommentResponse create(Long postId,CommentRequest req,Authentication auth){
   UserEntity user=current.require(auth); PostEntity post=posts.findActiveDetail(postId).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"게시글을 찾을 수 없습니다."));
   CommentEntity saved=comments.save(new CommentEntity(post,user,req.content())); post.increaseComments(); return CommentResponse.from(saved,user.getId()); }
 @Transactional public void delete(Long id,Authentication auth){
   UserEntity user=current.require(auth); CommentEntity c=comments.findDetail(id).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"댓글을 찾을 수 없습니다."));
   if(!c.getAuthor().getId().equals(user.getId())&&user.getRole()!=com.errorwiky.user.UserRole.ADMIN) throw new BusinessException(HttpStatus.FORBIDDEN,"댓글 삭제 권한이 없습니다.");
   if(!c.isDeleted()){c.delete();c.getPost().decreaseComments();}
 }
}
