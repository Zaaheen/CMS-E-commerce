package com.cmsecommerce.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxonomyResponseDTO {
    private UUID id;
    private String name;
    private String slug;
    private String type;
    private UUID parentId;
    private String parentName;
}
