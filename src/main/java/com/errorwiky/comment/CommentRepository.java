package com.errorwiky.comment;
import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface CommentRepository extends JpaRepository<CommentEntity,Long>{
 @EntityGraph(attributePaths="author") List<CommentEntity> findByPostIdOrderByCreatedAtAsc(Long postId);
 @Query("select c from CommentEntity c join fetch c.author join fetch c.post where c.id=:id") Optional<CommentEntity> findDetail(@Param("id") Long id);
}
