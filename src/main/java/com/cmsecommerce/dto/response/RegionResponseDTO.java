package com.cmsecommerce.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponseDTO {
    private UUID id;
    private String externalId;
    private String name;
    private String slug;
    private UUID parentId;
    private String parentName;
    private Integer level;
    private String path;
}
