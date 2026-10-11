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
public class TaxonomyTreeResponseDTO {

    private UUID id;
    private String name;
    private String slug;
    private String type;

    @Builder.Default
    private List<TaxonomyTreeResponseDTO> children = new ArrayList<>();
}
