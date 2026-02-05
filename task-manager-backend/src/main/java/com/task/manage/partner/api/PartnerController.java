package com.task.manage.partner.api;

import com.task.manage.partner.dto.PartnerRequestDto;
import com.task.manage.partner.dto.PartnerResponseDto;
import com.task.manage.partner.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @PostMapping
    public ResponseEntity<PartnerResponseDto> createPartner(@Valid @RequestBody PartnerRequestDto requestDto) {
        PartnerResponseDto response = partnerService.createPartner(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartnerResponseDto> updatePartner(
            @PathVariable Long id,
            @Valid @RequestBody PartnerRequestDto requestDto) {
        PartnerResponseDto response = partnerService.updatePartner(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponseDto> getPartnerById(
            @PathVariable Long id) {
        PartnerResponseDto response = partnerService.getPartnerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PartnerResponseDto>> getAllPartners() {
        List<PartnerResponseDto> response = partnerService.getAllPartners();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<PartnerResponseDto>> getAllPartnersPaginated(
            @PageableDefault(size = 10, sort = "dateCreated") Pageable pageable) {
        Page<PartnerResponseDto> response = partnerService.getAllPartnersPaginated(pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartner(
            @PathVariable Long id) {
        partnerService.deletePartner(id);
        return ResponseEntity.noContent().build();
    }
}
