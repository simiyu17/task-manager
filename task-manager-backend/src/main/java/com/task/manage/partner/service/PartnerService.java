package com.task.manage.partner.service;

import com.task.manage.partner.dto.PartnerRequestDto;
import com.task.manage.partner.dto.PartnerResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PartnerService {

    PartnerResponseDto createPartner(PartnerRequestDto requestDto);

    PartnerResponseDto updatePartner(Long id, PartnerRequestDto requestDto);

    PartnerResponseDto getPartnerById(Long id);

    List<PartnerResponseDto> getAllPartners();

    Page<PartnerResponseDto> getAllPartnersPaginated(Pageable pageable);

    void deletePartner(Long id);
}
