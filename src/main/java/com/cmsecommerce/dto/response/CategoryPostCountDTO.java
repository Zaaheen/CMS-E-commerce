package com.cmsecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CategoryPostCountDTO {
    private UUID categoryId;
    private String categoryName;
    private Long postCount;
}
