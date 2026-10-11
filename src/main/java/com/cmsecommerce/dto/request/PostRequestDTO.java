package com.cmsecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class PostRequestDTO {
    @NotBlank(message = "Tiêu đề bài viết không được để trống")
    private String title;

    private String slug;

    private String content;

    @Pattern(regexp = "^(DRAFT|PUBLISHED|ARCHIVED)$", message = "Trạng thái phải là DRAFT, PUBLISHED hoặc ARCHIVED")
    private String status;

    private Map<String, Object> customFields;

    private Set<UUID> taxonomyIds;

    private List<UUID> regionIds;
}
