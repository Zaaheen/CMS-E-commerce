package com.cmsecommerce.service.impl;

import com.cmsecommerce.dto.request.RegionRequestDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.RegionResponseDTO;
import com.cmsecommerce.dto.response.RegionTreeResponseDTO;
import com.cmsecommerce.entity.Region;
import com.cmsecommerce.exception.BadRequestException;
import com.cmsecommerce.exception.ResourceNotFoundException;
import com.cmsecommerce.repository.IRegionRepository;
import com.cmsecommerce.service.IRegionAdminService;
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
public class RegionAdminServiceImpl implements IRegionAdminService {

    private final IRegionRepository regionRepository;

    @Override
    @Transactional
    public RegionResponseDTO createRegion(RegionRequestDTO request) {
        String slug = generateValidSlug(request.getSlug(), request.getName(), null);

        Region parent = null;
        int level = 1;

        if (request.getParentId() != null) {
            parent = regionRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vùng miền cha với ID: " + request.getParentId()));
            level = parent.getLevel() + 1;
        }

        Region region = Region.builder()
                .externalId(request.getExternalId())
                .name(request.getName())
                .slug(slug)
                .parent(parent)
                .level(level)
                .build();

        Region savedRegion = regionRepository.save(region);

        // Tính toán chuỗi path chuẩn dạng: /parent_id/child_id/
        String path = (parent == null)
                ? "/" + savedRegion.getId() + "/"
                : parent.getPath() + savedRegion.getId() + "/";

        savedRegion.setPath(path);
        regionRepository.save(savedRegion);

        return mapToResponse(savedRegion);
    }

    @Override
    public RegionResponseDTO updateRegion(UUID id, RegionRequestDTO request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vùng miền với ID: " + id));

        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new BadRequestException("Vùng miền không thể làm cha của chính nó");
        }

        String slug = generateValidSlug(request.getSlug(), request.getName(), id);

        Region parent = null;
        int level = 1;

        if (request.getParentId() != null) {
            parent = regionRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vùng miền cha với ID: " + request.getParentId()));

            // Tránh vòng lặp phân cấp (Gán con làm cha)
            if (parent.getPath() != null && parent.getPath().contains("/" + id + "/")) {
                throw new BadRequestException("Không thể chọn vùng miền con làm vùng miền cha");
            }

            level = parent.getLevel() + 1;
        }

        String oldPath = region.getPath();
        region.updateInformation(request.getExternalId(), request.getName(), slug, parent, level);

        String newPath = (parent == null)
                ? "/" + region.getId() + "/"
                : parent.getPath() + region.getId() + "/";

        region.updatePath(newPath);
        Region updatedRegion = regionRepository.save(region);

        // Cập nhật lại level và path cho toàn bộ các vùng miền con cháu nếu parent thay đổi
        if (oldPath != null && !oldPath.equals(newPath)) {
            updateChildrenPathAndLevel(oldPath, newPath, level - (parent == null ? 1 : parent.getLevel() + 1));
        }

        return mapToResponse(updatedRegion);
    }

    @Override
    public void deleteRegion(UUID id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vùng miền với ID: " + id));

        if (region.getChildren() != null && !region.getChildren().isEmpty()) {
            for (Region child : region.getChildren()) {
                child.setParent(region.getParent());
                child.setLevel(region.getParent() == null ? 1 : region.getParent().getLevel() + 1);
            }
            regionRepository.saveAll(region.getChildren());
        }

        regionRepository.delete(region);
    }

    @Override
    public RegionResponseDTO getRegionById(UUID id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vùng miền với ID: " + id));
        return mapToResponse(region);
    }

    @Override
    public PageResponseDTO<RegionResponseDTO> getRegions(String keyword, Integer level, UUID parentId, Pageable pageable) {
        Page<Region> page = regionRepository.filterRegions(keyword, level, parentId, pageable);
        Page<RegionResponseDTO> responsePage = page.map(this::mapToResponse);
        return PageResponseDTO.from(responsePage);
    }

    @Override
    public List<RegionTreeResponseDTO> getRegionTree() {
        List<Region> rootRegions = regionRepository.findByParentIsNull();
        return rootRegions.stream()
                .map(this::mapToTreeResponse)
                .collect(Collectors.toList());
    }

    private void updateChildrenPathAndLevel(String oldPath, String newPath, int levelDelta) {
        List<Region> children = regionRepository.findByPathStartingWith(oldPath);
        for (Region child : children) {
            if (child.getPath().startsWith(oldPath)) {
                String updatedChildPath = child.getPath().replaceFirst(oldPath, newPath);
                child.setPath(updatedChildPath);
                child.setLevel(child.getLevel() + levelDelta);
            }
        }
        regionRepository.saveAll(children);
    }

    private String generateValidSlug(String customSlug, String name, UUID currentId) {
        String baseSlug = StringUtils.hasText(customSlug) ? SlugUtils.toSlug(customSlug) : SlugUtils.toSlug(name);
        String finalSlug = baseSlug;

        boolean exists = (currentId == null)
                ? regionRepository.existsBySlug(finalSlug)
                : regionRepository.existsBySlugAndIdNot(finalSlug, currentId);

        int count = 1;
        while (exists) {
            finalSlug = baseSlug + "-" + count;
            exists = (currentId == null)
                    ? regionRepository.existsBySlug(finalSlug)
                    : regionRepository.existsBySlugAndIdNot(finalSlug, currentId);
            count++;
        }

        return finalSlug;
    }

    private RegionResponseDTO mapToResponse(Region region) {
        return RegionResponseDTO.builder()
                .id(region.getId())
                .externalId(region.getExternalId())
                .name(region.getName())
                .slug(region.getSlug())
                .parentId(region.getParent() != null ? region.getParent().getId() : null)
                .parentName(region.getParent() != null ? region.getParent().getName() : null)
                .level(region.getLevel())
                .path(region.getPath())
                .build();
    }

    private RegionTreeResponseDTO mapToTreeResponse(Region region) {
        List<RegionTreeResponseDTO> childrenTree = (region.getChildren() != null)
                ? region.getChildren().stream().map(this::mapToTreeResponse).collect(Collectors.toList())
                : List.of();

        return RegionTreeResponseDTO.builder()
                .id(region.getId())
                .externalId(region.getExternalId())
                .name(region.getName())
                .slug(region.getSlug())
                .level(region.getLevel())
                .path(region.getPath())
                .children(childrenTree)
                .build();
    }
}
