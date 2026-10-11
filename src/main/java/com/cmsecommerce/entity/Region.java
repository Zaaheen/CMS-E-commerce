package com.cmsecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "regions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Region parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Region> children;

    private Integer level;

    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // Hàm domain helper giúp cập nhật dữ liệu
    public void updateInformation(String externalId, String name, String slug, Region parent, Integer level) {
        this.externalId = externalId;
        this.name = name;
        this.slug = slug;
        this.parent = parent;
        this.level = level;
    }
    public void updatePath(String path) {
        this.path = path;
    }
}
