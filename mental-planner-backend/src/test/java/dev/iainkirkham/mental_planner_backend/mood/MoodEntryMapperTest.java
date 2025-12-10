package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MoodEntryMapper.
 * Verifies DTO to entity conversions and vice versa.
 */
class MoodEntryMapperTest {

    private MoodEntryMapper mapper;
    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T00:00:00Z");
    private static final String TEST_USER_ID = "user_test123";

    @BeforeEach
    void setUp() {
        mapper = new MoodEntryMapper();
    }

    @Test
    void toEntity_ShouldConvertRequestDTOToEntity() {
        // Given
        MoodEntryRequestDTO requestDTO = new MoodEntryRequestDTO();
        requestDTO.setMoodScore((short) 4);
        requestDTO.setDateTime(FIXED_NOW);
        requestDTO.setFactors(List.of("Work", "Stress"));
        requestDTO.setNotes("Had a tough day.");

        // When
        MoodEntry entity = mapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // ID should not be set from DTO
        assertThat(entity.getUserId()).isNull(); // userId should not be set from DTO
        assertThat(entity.getMoodScore()).isEqualTo((short) 4);
        assertThat(entity.getDateTime()).isEqualTo(FIXED_NOW);
        assertThat(entity.getFactors()).containsExactly("Work", "Stress");
        assertThat(entity.getNotes()).isEqualTo("Had a tough day.");
    }

    @Test
    void toResponseDTO_ShouldConvertEntityToResponseDTO() {
        // Given
        MoodEntry entity = new MoodEntry();
        entity.setId(1L);
        entity.setMoodScore((short) 5);
        entity.setDateTime(FIXED_NOW);
        entity.setFactors(List.of("Exercise", "Sleep"));
        entity.setNotes("Feeling great!");
        entity.setUserId(TEST_USER_ID);

        // When
        MoodEntryResponseDTO responseDTO = mapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(1L);
        assertThat(responseDTO.getMoodScore()).isEqualTo((short) 5);
        assertThat(responseDTO.getDateTime()).isEqualTo(FIXED_NOW);
        assertThat(responseDTO.getFactors()).containsExactly("Exercise", "Sleep");
        assertThat(responseDTO.getNotes()).isEqualTo("Feeling great!");
        // userId should NOT be in response DTO
    }

    @Test
    void toResponseDTOList_ShouldConvertListOfEntities() {
        // Given
        MoodEntry entity1 = new MoodEntry();
        entity1.setId(1L);
        entity1.setMoodScore((short) 4);
        entity1.setDateTime(FIXED_NOW);
        entity1.setUserId(TEST_USER_ID);

        MoodEntry entity2 = new MoodEntry();
        entity2.setId(2L);
        entity2.setMoodScore((short) 5);
        entity2.setDateTime(FIXED_NOW.plusSeconds(3600));
        entity2.setUserId(TEST_USER_ID);

        List<MoodEntry> entities = List.of(entity1, entity2);

        // When
        List<MoodEntryResponseDTO> responseDTOs = mapper.toResponseDTOList(entities);

        // Then
        assertThat(responseDTOs).hasSize(2);
        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void updateEntityFromDTO_ShouldUpdateExistingEntity() {
        // Given
        MoodEntry existingEntity = new MoodEntry();
        existingEntity.setId(1L);
        existingEntity.setMoodScore((short) 3);
        existingEntity.setDateTime(FIXED_NOW);
        existingEntity.setFactors(List.of("Old"));
        existingEntity.setNotes("Old notes");
        existingEntity.setUserId(TEST_USER_ID);

        MoodEntryRequestDTO requestDTO = new MoodEntryRequestDTO();
        requestDTO.setMoodScore((short) 5);
        requestDTO.setDateTime(FIXED_NOW.plusSeconds(100));
        requestDTO.setFactors(List.of("New", "Better"));
        requestDTO.setNotes("Updated notes");

        // When
        mapper.updateEntityFromDTO(existingEntity, requestDTO);

        // Then
        assertThat(existingEntity.getId()).isEqualTo(1L); // ID should not change
        assertThat(existingEntity.getUserId()).isEqualTo(TEST_USER_ID); // userId should not change
        assertThat(existingEntity.getMoodScore()).isEqualTo((short) 5);
        assertThat(existingEntity.getDateTime()).isEqualTo(FIXED_NOW.plusSeconds(100));
        assertThat(existingEntity.getFactors()).containsExactly("New", "Better");
        assertThat(existingEntity.getNotes()).isEqualTo("Updated notes");
    }

    @Test
    void toEntity_WithNullDTO_ShouldReturnNull() {
        // When
        MoodEntry entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    void toResponseDTO_WithNullEntity_ShouldReturnNull() {
        // When
        MoodEntryResponseDTO responseDTO = mapper.toResponseDTO(null);

        // Then
        assertThat(responseDTO).isNull();
    }
}

