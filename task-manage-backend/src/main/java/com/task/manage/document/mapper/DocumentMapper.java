package com.task.manage.document.mapper;

import com.task.manage.document.domain.Document;
import com.task.manage.document.dto.DocumentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DocumentMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "documentType", expression = "java(documentTypeToString(document.getDocumentType()))")
    @Mapping(target = "fileLocation", expression = "java(fileLocationToString(document.getFileLocation()))")
    DocumentResponseDto toResponseDto(Document document);

    default String documentTypeToString(Document.DocumentType documentType) {
        return documentType != null ? documentType.name() : null;
    }

    default String fileLocationToString(Document.FileLocation fileLocation) {
        return fileLocation != null ? fileLocation.name() : null;
    }
}
