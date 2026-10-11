package com.cmsecommerce.repository;

import com.cmsecommerce.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Query("""
        SELECT DISTINCT p FROM Post p 
        LEFT JOIN p.taxonomies t 
        LEFT JOIN p.regions r 
        WHERE (:status IS NULL OR p.status = :status) 
        AND (:taxonomyId IS NULL OR t.id = :taxonomyId) 
        AND (:regionId IS NULL OR r.id = :regionId) 
        AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
             OR LOWER(CAST(p.content AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Post> filterPosts(@Param("status") String status,
                           @Param("taxonomyId") UUID taxonomyId,
                           @Param("regionId") UUID regionId,
                           @Param("keyword") String keyword,
                           Pageable pageable);
}
