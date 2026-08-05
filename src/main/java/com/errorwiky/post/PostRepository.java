package com.errorwiky.post;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    @Query("""
        select p from PostEntity p join fetch p.author
        where p.id=:id and p.deleted=false
    """) Optional<PostEntity> findActiveDetail(@Param("id") Long id);

    @EntityGraph(attributePaths="author")
    @Query("""
        select p from PostEntity p
        where p.deleted=false and p.boardType=:boardType
          and (:category is null or p.category=:category)
          and (:keyword is null or lower(p.title) like lower(concat('%',:keyword,'%')))
          and (:authorId is null or p.author.id=:authorId)
    """)
    Page<PostEntity> search(@Param("boardType") BoardType boardType,
                            @Param("category") ErrorCategory category,
                            @Param("keyword") String keyword,
                            @Param("authorId") Long authorId,
                            Pageable pageable);
}
