package com.cmsecommerce.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {
    private UUID id;
    private String title;
    private String slug;
    private String content;
    private String status;
    private OffsetDateTime publishedAt;
    private Map<String, Object> customFields;

    private UUID authorId;
    private String authorEmail;

    private List<TaxonomyResponseDTO> taxonomies;
    private List<RegionResponseDTO> regions;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
