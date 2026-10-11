package com.cmsecommerce.service;

import com.cmsecommerce.dto.request.PostRequestDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.PostResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IPostAdminService {
    PostResponseDTO createPost(PostRequestDTO request, UUID currentUserId);

    PostResponseDTO updatePost(UUID id, PostRequestDTO request);

    PostResponseDTO updatePostStatus(UUID id, String status);

    void deletePost(UUID id);

    PostResponseDTO getPostById(UUID id);

    PostResponseDTO getPostBySlug(String slug);

    PageResponseDTO<PostResponseDTO> getPosts(String status, UUID taxonomyId, UUID regionId, String keyword, Pageable pageable);
}
