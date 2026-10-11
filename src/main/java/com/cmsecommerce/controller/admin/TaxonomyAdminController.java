package com.cmsecommerce.controller.admin;

import com.cmsecommerce.dto.request.TaxonomyRequestDTO;
import com.cmsecommerce.dto.response.ApiResponse;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.TaxonomyResponseDTO;
import com.cmsecommerce.dto.response.TaxonomyTreeResponseDTO;
import com.cmsecommerce.service.ITaxonomyAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/taxonomies")
@RequiredArgsConstructor
public class TaxonomyAdminController {

    private final ITaxonomyAdminService taxonomyAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<TaxonomyResponseDTO>>> getAllTaxonomies(
            @RequestParam(defaultValue = "CATEGORY") String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = (Pageable) PageRequest.of(page - 1, size, sort);

        PageResponseDTO<TaxonomyResponseDTO> response = taxonomyAdminService.getTaxonomies(type, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<TaxonomyTreeResponseDTO>>> getTaxonomyTree(
            @RequestParam(defaultValue = "CATEGORY") String type
    ) {
        List<TaxonomyTreeResponseDTO> tree = taxonomyAdminService.getTaxonomyTree(type);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxonomyResponseDTO>> getTaxonomyById(@PathVariable UUID id) {
        TaxonomyResponseDTO response = taxonomyAdminService.getTaxonomyById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaxonomyResponseDTO>> createTaxonomy(@Valid @RequestBody TaxonomyRequestDTO request){
        TaxonomyResponseDTO response = taxonomyAdminService.createTaxonomy(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo danh mục/tag thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxonomyResponseDTO>> updateTaxonomy(
            @PathVariable UUID id,
            @Valid @RequestBody TaxonomyRequestDTO request
    ) {
        TaxonomyResponseDTO response = taxonomyAdminService.updateTaxonomy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục/tag thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTaxonomy(@PathVariable UUID id) {
        taxonomyAdminService.deleteTaxonomy(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục/tag thành công", null));
    }
}
