package com.task.manage.donor.service;

import com.task.manage.donor.dto.DonorRequestDto;
import com.task.manage.donor.dto.DonorResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DonorService {

    DonorResponseDto createDonor(DonorRequestDto requestDto);

    DonorResponseDto updateDonor(Long id, DonorRequestDto requestDto);

    DonorResponseDto getDonorById(Long id);

    List<DonorResponseDto> getAllDonors();

    Page<DonorResponseDto> getAllDonorsPaginated(Pageable pageable);

    void deleteDonor(Long id);
}

