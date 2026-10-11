package com.cmsecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // Thống kê bài viết
    private long totalPosts;
    private long publishedPosts;
    private long draftPosts;
    private long archivedPosts;

    // Thống kê người dùng & phân cấp
    private long totalUsers;
    private long totalTaxonomies;
    private long totalRegions;

    // Thống kê Media & Dung lượng Cloudinary
    private long totalMediaFiles;
    private long totalMediaSizeBytes;
    private String formattedTotalMediaSize;

    // Phân bố chi tiết
    private List<CategoryPostCountDTO> postsByCategory;
    private List<RegionPostCountDTO> postsByRegion;
    private List<PostResponseDTO> recentPosts;
}
