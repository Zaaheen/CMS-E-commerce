package com.cmsecommerce.service;

import com.cmsecommerce.dto.request.TaxonomyRequestDTO;
import com.cmsecommerce.dto.response.PageResponseDTO;
import com.cmsecommerce.dto.response.TaxonomyResponseDTO;
import com.cmsecommerce.dto.response.TaxonomyTreeResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ITaxonomyAdminService {

    TaxonomyResponseDTO createTaxonomy(TaxonomyRequestDTO request);

    TaxonomyResponseDTO updateTaxonomy(UUID id, TaxonomyRequestDTO request);

    void deleteTaxonomy(UUID id);

    TaxonomyResponseDTO getTaxonomyById(UUID id);

    PageResponseDTO<TaxonomyResponseDTO> getTaxonomies(String type, String keyword, Pageable pageable);

    List<TaxonomyTreeResponseDTO> getTaxonomyTree(String type);
}
