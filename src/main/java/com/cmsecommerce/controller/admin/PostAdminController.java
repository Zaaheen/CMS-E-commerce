package com.cmsecommerce.controller.admin;

import com.cmsecommerce.dto.request.PostRequestDTO;
import com.cmsecommerce.dto.response.ApiResponse;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.PostResponseDTO;
import com.cmsecommerce.security.user.CustomUserDetails;
import com.cmsecommerce.service.IPostAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PostAdminController {

    private final IPostAdminService postAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<PostResponseDTO>>> getPosts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID taxonomyId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        PageResponseDTO<PostResponseDTO> response = postAdminService.getPosts(status, taxonomyId, regionId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDTO>> getPostById(@PathVariable UUID id) {
        PostResponseDTO response = postAdminService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<PostResponseDTO>> getPostBySlug(@PathVariable String slug) {
        PostResponseDTO response = postAdminService.getPostBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDTO>> createPost(
            @Valid @RequestBody PostRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PostResponseDTO response = postAdminService.createPost(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo bài viết thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDTO>> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody PostRequestDTO request
    ) {
        PostResponseDTO response = postAdminService.updatePost(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết thành công", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PostResponseDTO>> updatePostStatus(
            @PathVariable UUID id,
            @RequestParam String status
    ) {
        PostResponseDTO response = postAdminService.updatePostStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái bài viết thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable UUID id) {
        postAdminService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", null));
    }
}
