package com.cmsecommerce.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionTreeResponseDTO {
    private UUID id;
    private String externalId;
    private String name;
    private String slug;
    private Integer level;
    private String path;

    @Builder.Default
    private List<RegionTreeResponseDTO> children = new ArrayList<>();
}
