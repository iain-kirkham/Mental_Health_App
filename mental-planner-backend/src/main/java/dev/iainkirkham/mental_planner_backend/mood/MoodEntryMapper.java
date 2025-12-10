package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between MoodEntry entities and DTOs.
 * Ensures proper separation between internal data model and API contract.
 */
@Component
public class MoodEntryMapper {

    /**
     * Converts a request DTO to an entity.
     * Note: id and userId are not set from the DTO and should be handled by the service layer.
     *
     * @param dto the request DTO
     * @return a new MoodEntry entity
     */
    public MoodEntry toEntity(MoodEntryRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        MoodEntry entity = new MoodEntry();
        entity.setMoodScore(dto.getMoodScore());
        entity.setDateTime(dto.getDateTime());
        entity.setFactors(dto.getFactors());
        entity.setNotes(dto.getNotes());

        return entity;
    }

    /**
     * Converts an entity to a response DTO.
     * Excludes sensitive fields like userId.
     *
     * @param entity the MoodEntry entity
     * @return a response DTO
     */
    public MoodEntryResponseDTO toResponseDTO(MoodEntry entity) {
        if (entity == null) {
            return null;
        }

        MoodEntryResponseDTO dto = new MoodEntryResponseDTO();
        dto.setId(entity.getId());
        dto.setMoodScore(entity.getMoodScore());
        dto.setDateTime(entity.getDateTime());
        dto.setFactors(entity.getFactors());
        dto.setNotes(entity.getNotes());

        return dto;
    }

    /**
     * Converts a list of entities to a list of response DTOs.
     *
     * @param entities list of MoodEntry entities
     * @return list of response DTOs
     */
    public List<MoodEntryResponseDTO> toResponseDTOList(List<MoodEntry> entities) {
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
    public void updateEntityFromDTO(MoodEntry entity, MoodEntryRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setMoodScore(dto.getMoodScore());
        entity.setDateTime(dto.getDateTime());
        entity.setFactors(dto.getFactors());
        entity.setNotes(dto.getNotes());
    }
}

