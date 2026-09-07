package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskTimeEntryMapper {

    public TaskTimeEntry toEntity(TaskTimeEntryRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        TaskTimeEntry entity = new TaskTimeEntry();
        entity.setTaskId(dto.getTaskId());
        entity.setStartedAt(dto.getStartedAt());
        entity.setEndedAt(dto.getEndedAt());
        entity.setMinutes(dto.getMinutes());
        entity.setEntryDate(dto.getEntryDate());
        entity.setSource(dto.getSource());
        entity.setNotes(dto.getNotes());
        entity.setScore(dto.getScore());
        entity.setEnergyRating(dto.getEnergyRating());

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
        dto.setNotes(entity.getNotes());
        dto.setScore(entity.getScore());
        dto.setEnergyRating(entity.getEnergyRating());

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
