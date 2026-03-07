package com.task.manage.task.mapper;

import com.task.manage.donor.domain.Donor;
import com.task.manage.donor.mapper.DonorMapper;
import com.task.manage.partner.domain.Partner;
import com.task.manage.partner.mapper.PartnerMapper;
import com.task.manage.task.domain.Task;
import com.task.manage.task.domain.Task.TaskStatus;
import com.task.manage.task.dto.TaskRequestDto;
import com.task.manage.task.dto.TaskResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = {PartnerMapper.class, DonorMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TaskMapper {

    @Mapping(target = "donor", source = "donor")
    @Mapping(target = "assignedPartner", source = "assignedPartner")
    @Mapping(target = "taskStatus", expression = "java(null != task.getTaskStatus() ? task.getTaskStatus().name() : null)")
    @Mapping(target = "taskStatusDisplayName", expression = "java(taskStatusToString(task.getTaskStatus()))")
    @Mapping(target = "stepValue", expression = "java(task.getTaskStatus() != null ? task.getTaskStatus().getStepValue() : null)")
    @Mapping(target = "possibleNextStatuses", expression = "java(task.getTaskPossibleNextStatuses())")
    TaskResponseDto toResponseDto(Task task);

    @Mapping(target = "donor", ignore = true)
    @Mapping(target = "assignedPartner", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "dataStatus", ignore = true)
    @Mapping(target = "taskStatus", expression = "java(stringToTaskStatus(\"INITIATED\"))")
    @Mapping(target = "requestReceivedAt", expression = "java(java.time.LocalDateTime.now())")
    Task toEntity(TaskRequestDto requestDto);

    @Mapping(target = "donor", ignore = true)
    @Mapping(target = "assignedPartner", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "dataStatus", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "allocateNotes", source = "allocateNotes")
    @Mapping(target = "acceptanceNotes", source = "acceptanceNotes")
    @Mapping(target = "rejectionNotes", source = "rejectionNotes")
    void updateEntityFromDto(TaskRequestDto requestDto, @MappingTarget Task task);

    default Partner mapPartnerFromId(Long partnerId) {
        if (partnerId == null) {
            return null;
        }
        Partner partner = new Partner();
        partner.setId(partnerId);
        return partner;
    }

    default Donor mapDonorFromId(Long donorId) {
        if (donorId == null) {
            return null;
        }
        Donor donor = new Donor();
        donor.setId(donorId);
        return donor;
    }

    default String taskStatusToString(TaskStatus taskStatus) {
        return taskStatus != null ? taskStatus.getDisplayName() : null;
    }

    default TaskStatus stringToTaskStatus(String taskStatus) {
        return TaskStatus.fromString(taskStatus);
    }
}
