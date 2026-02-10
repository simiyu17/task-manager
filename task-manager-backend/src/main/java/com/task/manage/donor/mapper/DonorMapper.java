package com.task.manage.donor.mapper;

import com.task.manage.donor.domain.Donor;
import com.task.manage.donor.dto.DonorRequestDto;
import com.task.manage.donor.dto.DonorResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DonorMapper {

    DonorResponseDto toResponseDto(Donor donor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "dataStatus", ignore = true)
    Donor toEntity(DonorRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "dataStatus", ignore = true)
    void updateEntityFromDto(DonorRequestDto requestDto, @MappingTarget Donor donor);
}

