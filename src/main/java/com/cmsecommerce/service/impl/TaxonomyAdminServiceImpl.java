package com.cmsecommerce.service.impl;

import com.cmsecommerce.dto.request.TaxonomyRequestDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.TaxonomyResponseDTO;
import com.cmsecommerce.dto.response.TaxonomyTreeResponseDTO;
import com.cmsecommerce.entity.Taxonomy;
import com.cmsecommerce.exception.BadRequestException;
import com.cmsecommerce.exception.ResourceNotFoundException;
import com.cmsecommerce.repository.ITaxonomyRepository;
import com.cmsecommerce.service.ITaxonomyAdminService;
import com.cmsecommerce.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxonomyAdminServiceImpl implements ITaxonomyAdminService {

    private final ITaxonomyRepository taxonomyRepository;

    @Override
    @Transactional
    public TaxonomyResponseDTO createTaxonomy(TaxonomyRequestDTO request) {
        // 1. Tạo slug hợp lệ và không trùng lặp
        String slug = generateValidSlug(request.getSlug(), request.getName(), null);
        // 2. Kiểm tra danh mục cha (nếu có)
        Taxonomy parent = null;
        if (request.getParentId() != null) {
            parent = taxonomyRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha với ID: " + request.getParentId()));
        }
        // 3. Khởi tạo Entity từ Request
        Taxonomy taxonomy = Taxonomy.builder()
                .name(request.getName())
                .slug(slug)
                .type(request.getType().toUpperCase())
                .parent(parent)
                .build();
        // 4. Lưu vào Database và chuyển thành DTO Response
        Taxonomy savedTaxonomy = taxonomyRepository.save(taxonomy);
        return mapToResponse(savedTaxonomy);
    }

    @Override
    @Transactional
    public TaxonomyResponseDTO updateTaxonomy(UUID id, TaxonomyRequestDTO request) {
        Taxonomy taxonomy = taxonomyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục/tag với ID: " + id));

        // Tránh gán chính nó làm danh mục cha
        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new BadRequestException("Danh mục không thể làm cha của chính nó");
        }

        String slug = generateValidSlug(request.getSlug(), request.getName(), id);

        Taxonomy parent = null;
        if (request.getParentId() != null) {
            parent = taxonomyRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha với ID: " + request.getParentId()));
        }

        taxonomy.updateInformation(request.getName(), slug, request.getType(), parent);

        Taxonomy updatedTaxonomy = taxonomyRepository.save(taxonomy);
        return mapToResponse(updatedTaxonomy);
    }

    @Override
    @Transactional
    public void deleteTaxonomy(UUID id) {
        Taxonomy taxonomy = taxonomyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục/tag với ID: " + id));

        // Xử lý các danh mục con: Đẩy chúng lên làm con của danh mục cha hiện tại
        if (taxonomy.getChildren() != null && !taxonomy.getChildren().isEmpty()) {
            for (Taxonomy child : taxonomy.getChildren()) {
                child.setParent(taxonomy.getParent());
            }
            taxonomyRepository.saveAll(taxonomy.getChildren());
        }

        taxonomyRepository.delete(taxonomy);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxonomyResponseDTO getTaxonomyById(UUID id) {
        Taxonomy taxonomy = taxonomyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục/tag với ID: " + id));
        return mapToResponse(taxonomy);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TaxonomyResponseDTO> getTaxonomies(String type, String keyword, Pageable pageable) {
        Page<Taxonomy> page;
        // Mặc định type là "CATEGORY" nếu không truyền
        String taxonomyType = type != null ? type.toUpperCase() : "CATEGORY";

        // Tìm kiếm theo tên (nếu có keyword) hoặc lấy toàn bộ theo type
        if (StringUtils.hasText(keyword)) {
            page = taxonomyRepository.findByTypeAndNameContainingIgnoreCase(taxonomyType, keyword, pageable);
        } else {
            page = taxonomyRepository.findByType(taxonomyType, pageable);
        }

        // Ánh xạ từng phần tử từ Entity -> DTO Response
        Page<TaxonomyResponseDTO> responsePage = page.map(this::mapToResponse);
        return PageResponseDTO.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxonomyTreeResponseDTO> getTaxonomyTree(String type) {
        String taxonomyType = type != null ? type.toUpperCase() : "CATEGORY";
        // Lấy các danh mục gốc (không có danh mục cha - parent IS NULL)
        List<Taxonomy> rootTaxonomies = taxonomyRepository.findByTypeAndParentIsNull(taxonomyType);

        // Chuyển đổi danh sách root thành dạng cây lồng nhau
        return rootTaxonomies.stream()
                .map(this::mapToTreeResponse)
                .collect(Collectors.toList());
    }

    // Các Helper Methods (Hàm bổ trợ)
    // Đảm bảo Slug không bị trùng
    private String generateValidSlug(String customSlug, String name, UUID currentId) {
        // Nếu truyền customSlug thì dùng, ngược lại tự tạo từ name
        String baseSlug = StringUtils.hasText(customSlug) ? SlugUtils.toSlug(customSlug) : SlugUtils.toSlug(name);
        String finalSlug = baseSlug;
        // Kiểm tra trùng lặp slug
        boolean exists = (currentId == null)
                ? taxonomyRepository.existsBySlug(finalSlug)
                : taxonomyRepository.existsBySlugAndIdNot(finalSlug, currentId);
        int counter = 1;
        // Nếu trùng, tự động thêm hậu tố -1, -2, -3... vào sau slug
        while (exists) {
            finalSlug = baseSlug + "-" + counter++;
            exists = (currentId == null)
                    ? taxonomyRepository.existsBySlug(finalSlug)
                    : taxonomyRepository.existsBySlugAndIdNot(finalSlug, currentId);
            counter++;
        }

        return finalSlug;
    }
    // Chuyển Entity sang DTO dạng phẳng
    private TaxonomyResponseDTO mapToResponse(Taxonomy taxonomy) {
        return TaxonomyResponseDTO.builder()
                .id(taxonomy.getId())
                .name(taxonomy.getName())
                .slug(taxonomy.getSlug())
                .type(taxonomy.getType())
                .parentId(taxonomy.getParent() != null ? taxonomy.getParent().getId() : null)
                .parentName(taxonomy.getParent() != null ? taxonomy.getParent().getName() : null)
                .build();
    }
    // Chuyển Entity sang DTO dạng Cây (Đệ quy)
    private TaxonomyTreeResponseDTO mapToTreeResponse(Taxonomy taxonomy) {
        List<TaxonomyTreeResponseDTO> childrenTree = (taxonomy.getChildren() != null)
                ? taxonomy.getChildren().stream().map(this::mapToTreeResponse).collect(Collectors.toList())
                : List.of();

        return TaxonomyTreeResponseDTO.builder()
                .id(taxonomy.getId())
                .name(taxonomy.getName())
                .slug(taxonomy.getSlug())
                .type(taxonomy.getType())
                .children(childrenTree)
                .build();
    }
}
