package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskTimeEntryMapper {

    public TaskTimeEntry toEntity(TaskTimeEntryRequestDTO dto, Long taskId) {
        if (dto == null) {
            return null;
        }

        TaskTimeEntry entity = new TaskTimeEntry();
        entity.setTaskId(taskId);
        entity.setStartedAt(dto.getStartedAt());
        entity.setEndedAt(dto.getEndedAt());
        entity.setMinutes(dto.getMinutes());
        entity.setEntryDate(dto.getEntryDate());
        entity.setSource(dto.getSource());
        entity.setNote(dto.getNote());

        return entity;
    }

    public TaskTimeEntryResponseDTO toResponseDTO(TaskTimeEntry entity) {
        if (entity == null) {
            return null;
        }

        TaskTimeEntryResponseDTO dto = new TaskTimeEntryResponseDTO();
        dto.setId(entity.getId());
        dto.setTaskId(entity.getTaskId());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());
        dto.setMinutes(entity.getMinutes());
        dto.setEntryDate(entity.getEntryDate());
        dto.setSource(entity.getSource());
        dto.setNote(entity.getNote());

        return dto;
    }

    public List<TaskTimeEntryResponseDTO> toResponseDTOList(List<TaskTimeEntry> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
