package com.task.manage.donor.service;

import com.task.manage.config.JwtTokenInspector;
import com.task.manage.donor.domain.Donor;
import com.task.manage.donor.domain.DonorRepository;
import com.task.manage.donor.dto.DonorRequestDto;
import com.task.manage.donor.dto.DonorResponseDto;
import com.task.manage.donor.exception.DonorNotFoundException;
import com.task.manage.donor.mapper.DonorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DonorServiceImpl implements DonorService {

    private final DonorRepository donorRepository;
    private final DonorMapper donorMapper;
    private final JwtTokenInspector jwtTokenInspector;

    @Override
    public DonorResponseDto createDonor(DonorRequestDto requestDto) {
        log.info("Creating donor with name: {}", requestDto.donorName());

        // Log JWT token claims to help debug
        log.debug("=== Inspecting JWT Token for Auditing ===");
        jwtTokenInspector.logAllTokenClaims();
        log.debug("Username that will be used for auditing: {}", jwtTokenInspector.getCurrentUsername());

        Donor donor = donorMapper.toEntity(requestDto);
        log.debug("Donor before save - createdBy: {}, lastModifiedBy: {}",
                donor.getCreatedBy(), donor.getLastModifiedBy());

        Donor savedDonor = donorRepository.save(donor);

        log.info("Donor created successfully with id: {}", savedDonor.getId());
        log.debug("Donor after save - createdBy: {}, lastModifiedBy: {}",
                savedDonor.getCreatedBy(), savedDonor.getLastModifiedBy());

        return donorMapper.toResponseDto(savedDonor);
    }

    @Override
    public DonorResponseDto updateDonor(Long id, DonorRequestDto requestDto) {
        log.info("Updating donor with id: {}", id);

        Donor existingDonor = donorRepository.findById(id)
                .orElseThrow(() -> new DonorNotFoundException(id));

        donorMapper.updateEntityFromDto(requestDto, existingDonor);
        Donor updatedDonor = donorRepository.save(existingDonor);

        log.info("Donor updated successfully with id: {}", updatedDonor.getId());
        return donorMapper.toResponseDto(updatedDonor);
    }

    @Override
    @Transactional(readOnly = true)
    public DonorResponseDto getDonorById(Long id) {
        log.info("Fetching donor with id: {}", id);

        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new DonorNotFoundException(id));

        return donorMapper.toResponseDto(donor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorResponseDto> getAllDonors() {
        log.info("Fetching all donors");

        List<Donor> donors = donorRepository.findAll();

        return donors.stream()
                .map(donorMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonorResponseDto> getAllDonorsPaginated(Pageable pageable) {
        log.info("Fetching all donors with pagination - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Donor> donorPage = donorRepository.findAll(pageable);

        return donorPage.map(donorMapper::toResponseDto);
    }

    @Override
    public void deleteDonor(Long id) {
        log.info("Deleting donor with id: {}", id);

        if (!donorRepository.existsById(id)) {
            throw new DonorNotFoundException(id);
        }

        donorRepository.deleteById(id);
        log.info("Donor deleted successfully with id: {}", id);
    }
}

