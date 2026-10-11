package com.cmsecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegionRequestDTO {
    private String externalId;

    @NotBlank(message = "Tên vùng miền không được để trống")
    private String name;

    private String slug;
    private UUID parentId;
}
