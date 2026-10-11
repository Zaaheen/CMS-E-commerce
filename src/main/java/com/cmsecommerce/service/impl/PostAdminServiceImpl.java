package com.cmsecommerce.service.impl;

import com.cmsecommerce.dto.request.PostRequestDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.PostResponseDTO;
import com.cmsecommerce.dto.response.RegionResponseDTO;
import com.cmsecommerce.dto.response.TaxonomyResponseDTO;
import com.cmsecommerce.entity.Post;
import com.cmsecommerce.entity.Region;
import com.cmsecommerce.entity.Taxonomy;
import com.cmsecommerce.entity.User;
import com.cmsecommerce.exception.ResourceNotFoundException;
import com.cmsecommerce.repository.IPostRepository;
import com.cmsecommerce.repository.IRegionRepository;
import com.cmsecommerce.repository.ITaxonomyRepository;
import com.cmsecommerce.repository.IUserRepository;
import com.cmsecommerce.service.IPostAdminService;
import com.cmsecommerce.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostAdminServiceImpl implements IPostAdminService {

    private final IPostRepository postRepository;
    private final IUserRepository userRepository;
    private final ITaxonomyRepository taxonomyRepository;
    private final IRegionRepository regionRepository;

    @Override
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO request, UUID currentUserId) {
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng ID: " + currentUserId));

        String slug = generateValidSlug(request.getSlug(), request.getTitle(), null);
        Set<Taxonomy> taxonomies = fetchTaxonomies(request.getTaxonomyIds());

        Post post = Post.builder()
                .author(author)
                .title(request.getTitle())
                .slug(slug)
                .content(request.getContent())
                .status(StringUtils.hasText(request.getStatus()) ? request.getStatus().toUpperCase() : "DRAFT")
                .customFields(request.getCustomFields())
                .taxonomies(taxonomies)
                .build();

        if ("PUBLISHED".equalsIgnoreCase(post.getStatus())) {
            post.changeStatus("PUBLISHED");
        }

        Post savedPost = postRepository.save(post);
        linkRegionsToPost(savedPost, request.getRegionIds());

        return mapToResponse(savedPost);
    }

    @Override
    @Transactional
    public PostResponseDTO updatePost(UUID id, PostRequestDTO request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với ID: " + id));

        String slug = generateValidSlug(request.getSlug(), request.getTitle(), id);
        Set<Taxonomy> taxonomies = fetchTaxonomies(request.getTaxonomyIds());

        // Rich Domain Model Method
        post.updateInfomation(request.getTitle(), slug, request.getContent(), request.getCustomFields(), taxonomies);

        if (StringUtils.hasText(request.getStatus())) {
            post.changeStatus(request.getStatus());
        }

        Post updatedPost = postRepository.save(post);
        linkRegionsToPost(updatedPost, request.getRegionIds());

        return mapToResponse(updatedPost);
    }

    @Override
    @Transactional
    public PostResponseDTO updatePostStatus(UUID id, String status) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với ID: " + id));

        post.changeStatus(status);
        Post updatedPost = postRepository.save(post);
        return mapToResponse(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với ID: " + id));

        // Hủy liên kết regions trước khi xóa
        if (post.getRegions() != null) {
            for (Region region : post.getRegions()) {
                region.setPost(null);
            }
            regionRepository.saveAll(post.getRegions());
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDTO getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với ID: " + id));
        return mapToResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDTO getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với slug: " + slug));
        return mapToResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PostResponseDTO> getPosts(String status, UUID taxonomyId, UUID regionId, String keyword, Pageable pageable) {
        Page<Post> page = postRepository.filterPosts(status, taxonomyId, regionId, keyword, pageable);
        Page<PostResponseDTO> responsePage = page.map(this::mapToResponse);
        return PageResponseDTO.from(responsePage);
    }


    private void linkRegionsToPost(Post post, List<UUID> regionIds) {
        if (regionIds == null) return;

        // Xóa liên kết bài viết khỏi các regions cũ
        if (post.getRegions() != null && !post.getRegions().isEmpty()) {
            for (Region oldRegion : post.getRegions()) {
                oldRegion.setPost(null);
            }
            regionRepository.saveAll(post.getRegions());
        }

        if (!regionIds.isEmpty()) {
            List<Region> newRegions = regionRepository.findAllById(regionIds);
            for (Region region : newRegions) {
                region.setPost(post);
            }
            regionRepository.saveAll(newRegions);
            post.setRegions(newRegions);
        }
    }

    private Set<Taxonomy> fetchTaxonomies(Set<UUID> taxonomyIds) {
        if (taxonomyIds == null || taxonomyIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(taxonomyRepository.findAllById(taxonomyIds));
    }

    private String generateValidSlug(String customSlug, String title, UUID currentId) {
        String baseSlug = StringUtils.hasText(customSlug) ? SlugUtils.toSlug(customSlug) : SlugUtils.toSlug(title);
        String finalSlug = baseSlug;

        boolean exists = (currentId == null)
                ? postRepository.existsBySlug(finalSlug)
                : postRepository.existsBySlugAndIdNot(finalSlug, currentId);

        int count = 1;
        while (exists) {
            finalSlug = baseSlug + "-" + count;
            exists = (currentId == null)
                    ? postRepository.existsBySlug(finalSlug)
                    : postRepository.existsBySlugAndIdNot(finalSlug, currentId);
            count++;
        }

        return finalSlug;
    }

    private PostResponseDTO mapToResponse(Post post) {
        List<TaxonomyResponseDTO> taxonomyResponses = post.getTaxonomies() != null
                ? post.getTaxonomies().stream().map(t -> TaxonomyResponseDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .slug(t.getSlug())
                .type(t.getType())
                .parentId(t.getParent() != null ? t.getParent().getId() : null)
                .parentName(t.getParent() != null ? t.getParent().getName() : null)
                .build()).toList()
                : List.of();

        List<RegionResponseDTO> regionResponses = post.getRegions() != null
                ? post.getRegions().stream().map(r -> RegionResponseDTO.builder()
                .id(r.getId())
                .externalId(r.getExternalId())
                .name(r.getName())
                .slug(r.getSlug())
                .parentId(r.getParent() != null ? r.getParent().getId() : null)
                .parentName(r.getParent() != null ? r.getParent().getName() : null)
                .level(r.getLevel())
                .path(r.getPath())
                .build()).toList()
                : List.of();

        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .customFields(post.getCustomFields())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .authorEmail(post.getAuthor() != null ? post.getAuthor().getEmail() : null)
                .taxonomies(taxonomyResponses)
                .regions(regionResponses)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
