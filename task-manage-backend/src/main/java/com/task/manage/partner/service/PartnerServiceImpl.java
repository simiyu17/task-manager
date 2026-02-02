package com.task.manage.partner.service;

import com.task.manage.config.JwtTokenInspector;
import com.task.manage.partner.domain.Partner;
import com.task.manage.partner.domain.PartnerRepository;
import com.task.manage.partner.dto.PartnerRequestDto;
import com.task.manage.partner.dto.PartnerResponseDto;
import com.task.manage.partner.exception.PartnerNotFoundException;
import com.task.manage.partner.mapper.PartnerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerMapper partnerMapper;
    private final JwtTokenInspector jwtTokenInspector;

    @Override
    public PartnerResponseDto createPartner(PartnerRequestDto requestDto) {
        log.info("Creating partner with name: {}", requestDto.partnerName());

        // Log JWT token claims to help debug
        log.debug("=== Inspecting JWT Token for Auditing ===");
        jwtTokenInspector.logAllTokenClaims();
        log.debug("Username that will be used for auditing: {}", jwtTokenInspector.getCurrentUsername());

        Partner partner = partnerMapper.toEntity(requestDto);
        log.debug("Partner before save - createdBy: {}, lastModifiedBy: {}",
                partner.getCreatedBy(), partner.getLastModifiedBy());

        Partner savedPartner = partnerRepository.save(partner);

        log.info("Partner created successfully with id: {}", savedPartner.getId());
        log.debug("Partner after save - createdBy: {}, lastModifiedBy: {}",
                savedPartner.getCreatedBy(), savedPartner.getLastModifiedBy());

        return partnerMapper.toResponseDto(savedPartner);
    }

    @Override
    public PartnerResponseDto updatePartner(Long id, PartnerRequestDto requestDto) {
        log.info("Updating partner with id: {}", id);

        Partner existingPartner = partnerRepository.findById(id)
                .orElseThrow(() -> new PartnerNotFoundException(id));

        partnerMapper.updateEntityFromDto(requestDto, existingPartner);
        Partner updatedPartner = partnerRepository.save(existingPartner);

        log.info("Partner updated successfully with id: {}", updatedPartner.getId());
        return partnerMapper.toResponseDto(updatedPartner);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerResponseDto getPartnerById(Long id) {
        log.info("Fetching partner with id: {}", id);

        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new PartnerNotFoundException(id));

        return partnerMapper.toResponseDto(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerResponseDto> getAllPartners() {
        log.info("Fetching all partners");

        List<Partner> partners = partnerRepository.findAll();

        return partners.stream()
                .map(partnerMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerResponseDto> getAllPartnersPaginated(Pageable pageable) {
        log.info("Fetching all partners with pagination - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Partner> partnerPage = partnerRepository.findAll(pageable);

        return partnerPage.map(partnerMapper::toResponseDto);
    }

    @Override
    public void deletePartner(Long id) {
        log.info("Deleting partner with id: {}", id);

        if (!partnerRepository.existsById(id)) {
            throw new PartnerNotFoundException(id);
        }

        partnerRepository.deleteById(id);
        log.info("Partner deleted successfully with id: {}", id);
    }
}
