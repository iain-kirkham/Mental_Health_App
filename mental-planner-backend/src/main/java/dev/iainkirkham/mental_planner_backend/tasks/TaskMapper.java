package dev.iainkirkham.mental_planner_backend.tasks;

import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper class for converting between Task entities and DTOs.
 * Ensures proper separation between internal data model and API contract.
 */
@Component
public class TaskMapper {

    /**
     * Converts a request DTO to an entity.
     * Note: id and userId are not set from the DTO and should be handled by the service layer.
     *
     * @param dto the request DTO
     * @return a new Task entity
     */
    public Task toEntity(TaskRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Task entity = new Task();
        updateEntityFromDTO(entity, dto);

        return entity;
    }

    /**
     * Converts an entity to a response DTO.
     * Excludes sensitive fields like userId.
     *
     * @param entity the Task entity
     * @return a response DTO
     */
    public TaskResponseDTO toResponseDTO(Task entity) {
        if (entity == null) {
            return null;
        }

        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setScheduledDate(entity.getScheduledDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setCompleted(entity.isCompleted());
        dto.setSortOrder(entity.getSortOrder());
        dto.setPlannedMinutes(entity.getPlannedMinutes());
        dto.setActualMinutes(entity.getActualMinutes());
        dto.setCategory(entity.getCategory());
        dto.setArchived(entity.isArchived());
        dto.setPriority(entity.getPriority());

        return dto;
    }

    /**
     * Converts a list of entities to a list of response DTOs.
     *
     * @param entities list of Task entities
     * @return list of response DTOs
     */
    public List<TaskResponseDTO> toResponseDTOList(List<Task> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Updates an existing entity with data from a request DTO.
     * Does not modify id or userId.
     *
     * @param entity the existing entity to update
     * @param dto the request DTO with new data
     */
    public void updateEntityFromDTO(Task entity, TaskRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setScheduledDate(dto.getScheduledDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setCompleted(dto.isCompleted());
        entity.setSortOrder(dto.getSortOrder());
        entity.setPlannedMinutes(dto.getPlannedMinutes());
        entity.recordTimerCheckpoint(dto.getActualMinutes());
        entity.setCategory(dto.getCategory());
        entity.setArchived(dto.isArchived());
        entity.setPriority(dto.getPriority() != null ? dto.getPriority() : TaskPriority.NORMAL);
    }
}
