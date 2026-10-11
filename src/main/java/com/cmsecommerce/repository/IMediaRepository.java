package com.cmsecommerce.repository;

import com.cmsecommerce.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IMediaRepository extends JpaRepository<Media, UUID> {

    @Query("SELECT m FROM Media m WHERE " +
            "(:fileType IS NULL OR LOWER(m.fileType) LIKE LOWER(CONCAT('%', :fileType, '%'))) AND " +
            "(:keyword IS NULL OR LOWER(m.altText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.filePath) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Media> filterMedia(@Param("fileType") String fileType,
                            @Param("keyword") String keyword,
                            Pageable pageable);

    // Truy vấn các media không gắn liên kết bài viết trong bảng posts và tạo trước mốc thời gian threshold (Orphan Media)
    @Query("""
        SELECT m FROM Media m 
        WHERE m.id NOT IN (SELECT p.id FROM Post p WHERE p.id = m.id) 
        AND m.createdAt < :threshold
    """)
    List<Media> findUnusedMediaBefore(@Param("threshold") OffsetDateTime threshold);
}
