package com.cmsecommerce.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponseDTO {
    private UUID id;
    private String publicId;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String altText;
    private UUID uploadedById;
    private String uploadedByEmail;
    private OffsetDateTime createdAt;
}
