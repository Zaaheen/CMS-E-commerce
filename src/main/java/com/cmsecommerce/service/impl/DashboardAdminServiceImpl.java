package com.cmsecommerce.service.impl;

import com.cmsecommerce.dto.response.CategoryPostCountDTO;
import com.cmsecommerce.dto.response.DashboardStatsResponse;
import com.cmsecommerce.dto.response.PostResponseDTO;
import com.cmsecommerce.dto.response.RegionPostCountDTO;
import com.cmsecommerce.entity.Post;
import com.cmsecommerce.repository.*;
import com.cmsecommerce.service.IDashboardAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardAdminServiceImpl implements IDashboardAdminService {

    private final IPostRepository postRepository;
    private final IUserRepository userRepository;
    private final ITaxonomyRepository taxonomyRepository;
    private final IRegionRepository regionRepository;
    private final IMediaRepository mediaRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        List<Post> allPosts = postRepository.findAll();

        long totalPosts = allPosts.size();
        long publishedPosts = allPosts.stream().filter(p -> "PUBLISHED".equalsIgnoreCase(p.getStatus())).count();
        long draftPosts = allPosts.stream().filter(p -> "DRAFT".equalsIgnoreCase(p.getStatus())).count();
        long archivedPosts = allPosts.stream().filter(p -> "ARCHIVED".equalsIgnoreCase(p.getStatus())).count();

        long totalUsers = userRepository.count();
        long totalTaxonomies = taxonomyRepository.count();
        long totalRegions = regionRepository.count();

        long totalMediaFiles = mediaRepository.count();
        long totalMediaSizeBytes = mediaRepository.findAll().stream()
                .mapToLong(m -> m.getFileSize() != null ? m.getFileSize() : 0L)
                .sum();

        // 5 bài viết mới nhất
        List<Post> recentPostEntities = postRepository.findAll(
                PageRequest.of(0, 5, Sort.by("createdAt").descending())
        ).getContent();

        List<PostResponseDTO> recentPosts = recentPostEntities.stream().map(this::mapToPostResponse).toList();

        // Thống kê phân bố bài viết theo danh mục (Sửa lỗi getPosts())
        List<CategoryPostCountDTO> postsByCategory = taxonomyRepository.findByTypeAndParentIsNull("CATEGORY")
                .stream()
                .map(t -> {
                    long count = allPosts.stream()
                            .filter(p -> p.getTaxonomies() != null && p.getTaxonomies().contains(t))
                            .count();
                    return CategoryPostCountDTO.builder()
                            .categoryId(t.getId())
                            .categoryName(t.getName())
                            .postCount(count)
                            .build();
                })
                .toList();

        // Thống kê phân bố bài viết theo danh mục
        List<CategoryPostCountDTO> postsByCategoryList = taxonomyRepository.findByTypeAndParentIsNull("CATEGORY")
                .stream()
                .map(t -> {
                    long count = allPosts.stream()
                            .filter(p -> p.getTaxonomies() != null && p.getTaxonomies().contains(t))
                            .count();
                    return CategoryPostCountDTO.builder()
                            .categoryId(t.getId())
                            .categoryName(t.getName())
                            .postCount(count)
                            .build();
                })
                .toList();

        // Thống kê phân bố bài viết theo vùng miền
        List<RegionPostCountDTO> postsByRegionList = regionRepository.findByParentIsNull()
                .stream()
                .map(r -> {
                    long count = allPosts.stream()
                            .filter(p -> p.getRegions() != null && p.getRegions().stream().anyMatch(reg -> r.getId().equals(reg.getId())))
                            .count();
                    return RegionPostCountDTO.builder()
                            .regionId(r.getId())
                            .regionName(r.getName())
                            .postCount(count)
                            .build();
                })
                .toList();

        return DashboardStatsResponse.builder()
                .totalPosts(totalPosts)
                .publishedPosts(publishedPosts)
                .draftPosts(draftPosts)
                .archivedPosts(archivedPosts)
                .totalUsers(totalUsers)
                .totalTaxonomies(totalTaxonomies)
                .totalRegions(totalRegions)
                .totalMediaFiles(totalMediaFiles)
                .totalMediaSizeBytes(totalMediaSizeBytes)
                .formattedTotalMediaSize(formatFileSize(totalMediaSizeBytes))
                .postsByCategory(postsByCategoryList)
                .postsByRegion(postsByRegionList)
                .recentPosts(recentPosts)
                .build();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %cB", bytes / Math.pow(1024, exp), pre);
    }

    private PostResponseDTO mapToPostResponse(Post post) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .authorEmail(post.getAuthor() != null ? post.getAuthor().getEmail() : null)
                .build();
    }
}
