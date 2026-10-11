package com.cmsecommerce.service;

import com.cmsecommerce.dto.response.MediaResponseDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IMediaAdminService {

    MediaResponseDTO uploadFile(MultipartFile file, String altText, UUID currentUserId);

    List<MediaResponseDTO> uploadMultipleFiles(List<MultipartFile> files, UUID currentUserId);

    PageResponseDTO<MediaResponseDTO> getMediaFiles(String fileType, String keyword, Pageable pageable);

    MediaResponseDTO getMediaById(UUID id);

    void deleteMedia(UUID id);

    int cleanupOrphanMediaFiles();
}
