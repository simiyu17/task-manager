package com.task.manage.donor.api;

import com.task.manage.donor.dto.DonorRequestDto;
import com.task.manage.donor.dto.DonorResponseDto;
import com.task.manage.donor.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;

    @PostMapping
    public ResponseEntity<DonorResponseDto> createDonor(@Valid @RequestBody DonorRequestDto requestDto) {
        DonorResponseDto response = donorService.createDonor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonorResponseDto> updateDonor(
            @PathVariable Long id,
            @Valid @RequestBody DonorRequestDto requestDto) {
        DonorResponseDto response = donorService.updateDonor(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorResponseDto> getDonorById(
            @PathVariable Long id) {
        DonorResponseDto response = donorService.getDonorById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DonorResponseDto>> getAllDonors() {
        List<DonorResponseDto> response = donorService.getAllDonors();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<DonorResponseDto>> getAllDonorsPaginated(
            @PageableDefault(size = 10, sort = "dateCreated") Pageable pageable) {
        Page<DonorResponseDto> response = donorService.getAllDonorsPaginated(pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDonor(
            @PathVariable Long id) {
        donorService.deleteDonor(id);
        return ResponseEntity.noContent().build();
    }
}

