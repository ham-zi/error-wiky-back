package com.errorwiky.post;

import com.errorwiky.auth.CurrentUserService;
import com.errorwiky.common.*;
import com.errorwiky.user.UserEntity;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostService {
    private final PostRepository posts; private final AttachmentRepository attachments; private final PostLikeRepository likes;
    private final CurrentUserService current; private final FileStorageService storage;
    public PostService(PostRepository p,AttachmentRepository a,PostLikeRepository l,CurrentUserService c,FileStorageService s){posts=p;attachments=a;likes=l;current=c;storage=s;}

    @Transactional(readOnly=true)
    public PageResponse<PostSummaryResponse> list(BoardType boardType,ErrorCategory category,String keyword,boolean mine,int page,int size,Authentication auth){
        Long authorId=mine?current.require(auth).getId():null;
        String q=keyword==null||keyword.isBlank()?null:keyword.trim();
        Pageable pageable=PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),50),Sort.by(Sort.Direction.DESC,"createdAt"));
        return PageResponse.from(posts.search(boardType,category,q,authorId,pageable).map(PostSummaryResponse::from));
    }

    @Transactional
    public PostDetailResponse detail(Long id,Authentication auth){
        PostEntity p=requirePost(id); p.increaseViews(); return detailResponse(p,auth);
    }

    @Transactional
    public PostDetailResponse create(PostRequest request,List<MultipartFile> files,Authentication auth){
        UserEntity user=current.require(auth); PostEntity p=posts.save(PostEntity.create(request,user));
        attachments.saveAll(storage.store(p,files)); return detailResponse(p,auth);
    }

    @Transactional
    public PostDetailResponse update(Long id,PostRequest request,List<MultipartFile> files,Authentication auth){
        UserEntity user=current.require(auth); PostEntity p=requirePost(id); requireOwner(p,user); p.apply(request);
        if(files!=null&&!files.isEmpty()) attachments.saveAll(storage.store(p,files)); return detailResponse(p,auth);
    }

    @Transactional
    public void delete(Long id,Authentication auth){UserEntity u=current.require(auth);PostEntity p=requirePost(id);requireOwner(p,u);p.delete();}

    @Transactional
    public Map<String,Object> toggleLike(Long id,Authentication auth){
        UserEntity u=current.require(auth); PostEntity p=requirePost(id); Optional<PostLikeEntity> found=likes.findByPostIdAndUserId(id,u.getId());
        boolean liked;
        if(found.isPresent()){likes.delete(found.get());p.decreaseLikes();liked=false;} else {likes.save(new PostLikeEntity(p,u));p.increaseLikes();liked=true;}
        return Map.of("liked",liked,"likeCount",p.getLikeCount());
    }

    private PostDetailResponse detailResponse(PostEntity p,Authentication auth){
        UserEntity u=current.optional(auth); Long uid=u==null?null:u.getId(); boolean liked=uid!=null&&likes.existsByPostIdAndUserId(p.getId(),uid);
        boolean editable=uid!=null&&(uid.equals(p.getAuthor().getId())||u.getRole()==com.errorwiky.user.UserRole.ADMIN);
        List<AttachmentResponse> atts=attachments.findByPostIdOrderByIdAsc(p.getId()).stream().map(AttachmentResponse::from).toList();
        return new PostDetailResponse(p.getId(),p.getBoardType(),p.getTitle(),p.getContent(),p.getCategory(),p.getErrorMessage(),p.getEnvironment(),p.getCause(),p.getSolution(),p.getAuthor().getId(),p.getAuthor().getName(),p.getViewCount(),p.getLikeCount(),p.getCommentCount(),liked,editable,p.getCreatedAt(),p.getUpdatedAt(),atts);
    }
    private PostEntity requirePost(Long id){return posts.findActiveDetail(id).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"게시글을 찾을 수 없습니다."));}
    private void requireOwner(PostEntity p,UserEntity u){if(!p.getAuthor().getId().equals(u.getId())&&u.getRole()!=com.errorwiky.user.UserRole.ADMIN)throw new BusinessException(HttpStatus.FORBIDDEN,"게시글 수정 권한이 없습니다.");}
}
