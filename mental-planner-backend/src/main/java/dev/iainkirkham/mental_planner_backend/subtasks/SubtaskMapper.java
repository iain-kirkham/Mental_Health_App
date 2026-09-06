package dev.iainkirkham.mental_planner_backend.subtasks;

import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubtaskMapper {

    public Subtask toEntity(SubtaskRequestDTO dto, Long taskId) {
        if (dto == null) {
            return null;
        }

        Subtask entity = new Subtask();
        entity.setTaskId(taskId);
        updateEntityFromDTO(entity, dto);

        return entity;
    }

    public SubtaskResponseDTO toResponseDTO(Subtask entity) {
        if (entity == null) {
            return null;
        }

        SubtaskResponseDTO dto = new SubtaskResponseDTO();
        dto.setId(entity.getId());
        dto.setTaskId(entity.getTaskId());
        dto.setTitle(entity.getTitle());
        dto.setCompleted(entity.isCompleted());
        dto.setSortOrder(entity.getSortOrder());
        dto.setPlannedMinutes(entity.getPlannedMinutes());

        return dto;
    }

    public List<SubtaskResponseDTO> toResponseDTOList(List<Subtask> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public void updateEntityFromDTO(Subtask entity, SubtaskRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setTitle(dto.getTitle());
        entity.setCompleted(dto.isCompleted());
        entity.setSortOrder(dto.getSortOrder());
        entity.setPlannedMinutes(dto.getPlannedMinutes());
    }
}
