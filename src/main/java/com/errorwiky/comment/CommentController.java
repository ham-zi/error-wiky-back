package com.errorwiky.comment;
import com.errorwiky.common.ApiResponse; import jakarta.validation.Valid; import java.util.List; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class CommentController{
 private final CommentService service; public CommentController(CommentService s){service=s;}
 @GetMapping("/posts/{postId}/comments") ApiResponse<List<CommentResponse>> list(@PathVariable(name = "postId") Long postId,Authentication a){return ApiResponse.ok(service.list(postId,a));}
 @PostMapping("/posts/{postId}/comments") ApiResponse<CommentResponse> create(@PathVariable(name = "postId") Long postId,@Valid @RequestBody CommentRequest r,Authentication a){return ApiResponse.ok(service.create(postId,r,a));}
 @DeleteMapping("/comments/{id}") ApiResponse<Void> delete(@PathVariable(name = "id") Long id,Authentication a){service.delete(id,a);return ApiResponse.ok("댓글을 삭제했습니다.");}
}
