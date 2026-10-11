package com.cmsecommerce.service;

import com.cmsecommerce.dto.request.RegionRequestDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.RegionResponseDTO;
import com.cmsecommerce.dto.response.RegionTreeResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IRegionAdminService {

    RegionResponseDTO createRegion(RegionRequestDTO request);

    RegionResponseDTO updateRegion(UUID id, RegionRequestDTO request);

    void deleteRegion(UUID id);

    RegionResponseDTO getRegionById(UUID id);

    PageResponseDTO<RegionResponseDTO> getRegions(String keyword, Integer level, UUID parentId, Pageable pageable);

    List<RegionTreeResponseDTO> getRegionTree();
}
