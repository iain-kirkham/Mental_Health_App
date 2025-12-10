package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionRequestDTO;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between PomodoroSession entities and DTOs.
 * Ensures proper separation between internal data model and API contract.
 */
@Component
public class PomodoroSessionMapper {

    /**
     * Converts a request DTO to an entity.
     * Note: id and userId are not set from the DTO and should be handled by the service layer.
     *
     * @param dto the request DTO
     * @return a new PomodoroSession entity
     */
    public PomodoroSession toEntity(PomodoroSessionRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        PomodoroSession entity = new PomodoroSession();
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setDuration(dto.getDuration());
        entity.setScore(dto.getScore());
        entity.setNotes(dto.getNotes());

        return entity;
    }

    /**
     * Converts an entity to a response DTO.
     * Excludes sensitive fields like userId.
     *
     * @param entity the PomodoroSession entity
     * @return a response DTO
     */
    public PomodoroSessionResponseDTO toResponseDTO(PomodoroSession entity) {
        if (entity == null) {
            return null;
        }

        PomodoroSessionResponseDTO dto = new PomodoroSessionResponseDTO();
        dto.setId(entity.getId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setDuration(entity.getDuration());
        dto.setScore(entity.getScore());
        dto.setNotes(entity.getNotes());

        return dto;
    }

    /**
     * Converts a list of entities to a list of response DTOs.
     *
     * @param entities list of PomodoroSession entities
     * @return list of response DTOs
     */
    public List<PomodoroSessionResponseDTO> toResponseDTOList(List<PomodoroSession> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing entity with data from a request DTO.
     * Does not modify id or userId.
     *
     * @param entity the existing entity to update
     * @param dto the request DTO with new data
     */
    public void updateEntityFromDTO(PomodoroSession entity, PomodoroSessionRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setDuration(dto.getDuration());
        entity.setScore(dto.getScore());
        entity.setNotes(dto.getNotes());
    }
}

