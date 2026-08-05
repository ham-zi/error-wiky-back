package com.errorwiky.post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AttachmentRepository extends JpaRepository<AttachmentEntity,Long>{
    List<AttachmentEntity> findByPostIdOrderByIdAsc(Long postId);
    void deleteByPostId(Long postId);
}
