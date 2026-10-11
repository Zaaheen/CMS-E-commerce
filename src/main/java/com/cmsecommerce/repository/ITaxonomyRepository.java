package com.cmsecommerce.repository;

import com.cmsecommerce.entity.Taxonomy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaxonomyRepository extends JpaRepository<Taxonomy, UUID> {

    Optional<Taxonomy> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Page<Taxonomy> findByType(String type, Pageable pageable);

    Page<Taxonomy> findByTypeAndNameContainingIgnoreCase(String type, String name, Pageable pageable);

    // Lấy tất cả danh mục gốc (parent is null) theo type
    List<Taxonomy> findByTypeAndParentIsNull(String type);
}
