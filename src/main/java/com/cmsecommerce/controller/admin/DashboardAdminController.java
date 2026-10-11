package com.cmsecommerce.controller.admin;

import com.cmsecommerce.dto.response.ApiResponse;
import com.cmsecommerce.dto.response.DashboardStatsResponse;
import com.cmsecommerce.service.IDashboardAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "APIs Thống kê chỉ số tổng quan cho Admin CMS")
public class DashboardAdminController {

    private final IDashboardAdminService dashboardAdminService;

    @GetMapping("/stats")
    @Operation(summary = "Lấy dữ liệu thống kê tổng quan", description = "Trả về chỉ số tổng số bài viết, trạng thái, media, phân bố danh mục và vùng miền")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse response = dashboardAdminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thống kê thành công", response));
    }
}
