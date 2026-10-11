package com.cmsecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "taxonomies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Taxonomy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 50)
    private String type; // 'CATEGORY', 'TAG'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Taxonomy parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Taxonomy> children;

    // Hàm domain helper giúp cập nhật dữ liệu
    public void updateInformation(String name, String slug, String type, Taxonomy parent) {
        this.name = name;
        this.slug = slug;
        this.type = type != null ? type.toUpperCase() : this.type;
        this.parent = parent;
    }
}
