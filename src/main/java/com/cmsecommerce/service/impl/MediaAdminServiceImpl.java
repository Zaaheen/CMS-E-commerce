package com.cmsecommerce.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cmsecommerce.dto.response.MediaResponseDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.entity.Media;
import com.cmsecommerce.entity.User;
import com.cmsecommerce.exception.BadRequestException;
import com.cmsecommerce.exception.ResourceNotFoundException;
import com.cmsecommerce.repository.IMediaRepository;
import com.cmsecommerce.repository.IUserRepository;
import com.cmsecommerce.service.IMediaAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaAdminServiceImpl implements IMediaAdminService {

    private final IMediaRepository mediaRepository;
    private final IUserRepository userRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public MediaResponseDTO uploadFile(MultipartFile file, String altText, UUID currentUserId) {
        if (file.isEmpty()) {
            throw new BadRequestException("Tệp tải lên không được để trống");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng ID: " + currentUserId));

        try {
            // Upload file lên Cloudinary trong thư mục 'cms_ecommerce'
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "cms_ecommerce",
                    "resource_type", "auto"
            ));

            String publicId = uploadResult.get("public_id").toString();
            String secureUrl = uploadResult.get("secure_url").toString();
            Long fileSize = ((Number) uploadResult.get("bytes")).longValue();
            String format = uploadResult.get("format") != null ? uploadResult.get("format").toString() : file.getContentType();

            Media media = Media.builder()
                    .publicId(publicId)
                    .filePath(secureUrl)
                    .fileType(format)
                    .fileSize(fileSize)
                    .altText(altText != null ? altText : file.getOriginalFilename())
                    .uploadedBy(user)
                    .build();

            Media savedMedia = mediaRepository.save(media);
            return mapToResponse(savedMedia);

        } catch (IOException e) {
            log.error("Lỗi khi tải tệp lên Cloudinary: {}", e.getMessage(), e);
            throw new BadRequestException("Tải tệp lên Cloudinary thất bại!");
        }
    }

    @Override
    @Transactional
    public List<MediaResponseDTO> uploadMultipleFiles(List<MultipartFile> files, UUID currentUserId) {
        List<MediaResponseDTO> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                responses.add(uploadFile(file, null, currentUserId));
            }
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<MediaResponseDTO> getMediaFiles(String fileType, String keyword, Pageable pageable) {
        Page<Media> page = mediaRepository.filterMedia(fileType, keyword, pageable);
        Page<MediaResponseDTO> responsePage = page.map(this::mapToResponse);
        return PageResponseDTO.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public MediaResponseDTO getMediaById(UUID id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tệp media với ID: " + id));
        return mapToResponse(media);
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteMedia(UUID id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tệp media với ID: " + id));

        try {
            // Xóa file trên Cloudinary bằng publicId
            cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.emptyMap());
            mediaRepository.delete(media);
            log.info("Đã xóa tệp media ID: {} (public_id: {}) khỏi Cloudinary và Database", id, media.getPublicId());
        } catch (IOException e) {
            log.error("Lỗi khi xóa tệp trên Cloudinary: {}", e.getMessage(), e);
            throw new BadRequestException("Xóa tệp trên Cloudinary thất bại!");
        }
    }

    @Override
    @Transactional
    public int cleanupOrphanMediaFiles() {
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(24);
        List<Media> orphanMediaList = mediaRepository.findUnusedMediaBefore(threshold);
        log.info("Phát hiện {} tệp media rác chưa liên kết (tạo trước 24h)", orphanMediaList.size());

        int count = 0;
        for (Media media : orphanMediaList) {
            try {
                cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.emptyMap());
                mediaRepository.delete(media);
                count++;
            } catch (Exception e) {
                log.error("Lỗi khi CronJob xóa tệp rác {}: {}", media.getPublicId(), e.getMessage());
            }
        }

        return count;
    }

    private MediaResponseDTO mapToResponse(Media media) {
        return MediaResponseDTO.builder()
                .id(media.getId())
                .publicId(media.getPublicId())
                .filePath(media.getFilePath())
                .fileType(media.getFileType())
                .fileSize(media.getFileSize())
                .altText(media.getAltText())
                .uploadedById(media.getUploadedBy() != null ? media.getUploadedBy().getId() : null)
                .uploadedByEmail(media.getUploadedBy() != null ? media.getUploadedBy().getEmail() : null)
                .createdAt(media.getCreatedAt())
                .build();
    }
}
