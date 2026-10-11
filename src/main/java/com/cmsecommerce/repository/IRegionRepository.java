package com.cmsecommerce.repository;

import com.cmsecommerce.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IRegionRepository extends JpaRepository<Region, UUID> {

    Optional<Region> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    // Lấy các vùng gốc (Cấp Tỉnh/Thành phố)
    List<Region> findByParentIsNull();

    // Tìm kiếm phân trang có điều kiện - native query vì external_id là bytea
    @Query(value = """
        SELECT * FROM regions r
        WHERE (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
               OR LOWER(r.external_id::text) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:level IS NULL OR r.level = :level)
          AND (:parentId IS NULL OR r.parent_id = :parentId)
        ORDER BY r.name
        """, countQuery = """
        SELECT COUNT(*) FROM regions r
        WHERE (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
               OR LOWER(r.external_id::text) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:level IS NULL OR r.level = :level)
          AND (:parentId IS NULL OR r.parent_id = :parentId)
        """, nativeQuery = true)
    Page<Region> filterRegions(@Param("keyword") String keyword,
                               @Param("level") Integer level,
                               @Param("parentId") UUID parentId,
                               Pageable pageable);

    // Lấy toàn bộ danh sách con cháu theo đường dẫn path
    List<Region> findByPathStartingWith(String path);
}
