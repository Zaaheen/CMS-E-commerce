package com.cmsecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 30, nullable = false)
    private String status; // 'DRAFT', 'PUBLISHED', 'ARCHIVED'

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private Map<String, Object> customFields;

    @ManyToMany
    @JoinTable(
            name = "post_taxonomies",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "taxonomy_id")
    )
    @Builder.Default
    private Set<Taxonomy> taxonomies = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Region> regions = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        if (this.status == null) {
            this.status = "DRAFT";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Rich Domain Method: Cập nhật thông tin chính của bài viết
     */
    public void updateInfomation(String title, String slug, String content, Map<String, Object> customFields, Set<Taxonomy> taxonomies) {
        this.title = title;
        this.slug = slug;
        this.content = content;
        this.customFields = customFields;
        this.taxonomies = taxonomies;
    }

    /**
     * Rich Domain Method: Đổi trạng thái bài viết
     */
    public void changeStatus(String newStatus) {
        this.status = newStatus.toUpperCase();
        if ("PUBLISHED".equalsIgnoreCase(newStatus) && this.publishedAt == null) {
            this.publishedAt = OffsetDateTime.now();
        }
    }
}
