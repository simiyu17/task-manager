package com.task.manage.partner.mapper;

import com.task.manage.partner.domain.Partner;
import com.task.manage.partner.dto.PartnerRequestDto;
import com.task.manage.partner.dto.PartnerResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PartnerMapper {

    PartnerResponseDto toResponseDto(Partner partner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "dataStatus", ignore = true)
    Partner toEntity(PartnerRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "dataStatus", ignore = true)
    void updateEntityFromDto(PartnerRequestDto requestDto, @MappingTarget Partner partner);
}
