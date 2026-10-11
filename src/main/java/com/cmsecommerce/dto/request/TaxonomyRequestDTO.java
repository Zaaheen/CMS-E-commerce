package com.cmsecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TaxonomyRequestDTO {

    @NotBlank(message = "Tên danh mục/tag không được để trống")
    private String name;

    private String slug; // Nếu để trống sẽ tự động sinh từ name

    @NotBlank(message = "Loại taxonomy không được để trống")
    @Pattern(regexp = "^(CATEGORY|TAG)$", message = "Loại taxonomy phải là CATEGORY hoặc TAG")
    private String type;

    private UUID parentId; // Có thể null đối với danh mục gốc hoặc Tag
}
