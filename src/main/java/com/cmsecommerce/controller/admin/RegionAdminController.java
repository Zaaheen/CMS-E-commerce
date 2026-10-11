package com.cmsecommerce.controller.admin;

import com.cmsecommerce.dto.request.RegionRequestDTO;
import com.cmsecommerce.dto.response.ApiResponse;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.RegionResponseDTO;
import com.cmsecommerce.dto.response.RegionTreeResponseDTO;
import com.cmsecommerce.service.IRegionAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/regions")
@RequiredArgsConstructor
public class RegionAdminController {
    private final IRegionAdminService regionAdminService;
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<RegionResponseDTO>>> getRegions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        PageResponseDTO<RegionResponseDTO> response = regionAdminService.getRegions(keyword, level, parentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<RegionTreeResponseDTO>>> getRegionTree() {
        List<RegionTreeResponseDTO> tree = regionAdminService.getRegionTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegionResponseDTO>> getRegionById(@PathVariable UUID id) {
        RegionResponseDTO response = regionAdminService.getRegionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegionResponseDTO>> createRegion(@Valid @RequestBody RegionRequestDTO request) {
        RegionResponseDTO response = regionAdminService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo vùng miền thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RegionResponseDTO>> updateRegion(
            @PathVariable UUID id,
            @Valid @RequestBody RegionRequestDTO request
    ) {
        RegionResponseDTO response = regionAdminService.updateRegion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vùng miền thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRegion(@PathVariable UUID id) {
        regionAdminService.deleteRegion(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa vùng miền thành công", null));
    }
}
