package com.cmsecommerce.controller.admin;

import com.cmsecommerce.dto.response.ApiResponse;
import com.cmsecommerce.dto.response.MediaResponseDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.security.user.CustomUserDetails;
import com.cmsecommerce.service.IMediaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/media")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MediaAdminController {

    private final IMediaAdminService mediaAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<MediaResponseDTO>>> getMediaFiles(
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        PageResponseDTO<MediaResponseDTO> response = mediaAdminService.getMediaFiles(fileType, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaResponseDTO>> getMediaById(@PathVariable UUID id) {
        MediaResponseDTO response = mediaAdminService.getMediaById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaResponseDTO>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String altText,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MediaResponseDTO response = mediaAdminService.uploadFile(file, altText, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tải tệp lên Cloudinary thành công", response));
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<MediaResponseDTO>>> uploadMultipleFiles(
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<MediaResponseDTO> response = mediaAdminService.uploadMultipleFiles(files, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tải danh sách tệp lên Cloudinary thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(@PathVariable UUID id) {
        mediaAdminService.deleteMedia(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tệp media khỏi Cloudinary thành công", null));
    }

    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanupOrphanMedia() {
        int cleanedCount = mediaAdminService.cleanupOrphanMediaFiles();
        return ResponseEntity.ok(ApiResponse.success("Dọn dẹp thành công " + cleanedCount + " tệp media rác trên Cloudinary", null));
    }
}
