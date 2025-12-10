package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionRequestDTO;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PomodoroSessionMapper.
 * Verifies DTO to entity conversions and vice versa.
 */
class PomodoroSessionMapperTest {

    private PomodoroSessionMapper mapper;
    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T00:00:00Z");
    private static final String TEST_USER_ID = "user_test123";

    @BeforeEach
    void setUp() {
        mapper = new PomodoroSessionMapper();
    }

    @Test
    void toEntity_ShouldConvertRequestDTOToEntity() {
        // Given
        PomodoroSessionRequestDTO requestDTO = new PomodoroSessionRequestDTO();
        requestDTO.setStartTime(FIXED_NOW);
        requestDTO.setEndTime(FIXED_NOW.plusSeconds(1500));
        requestDTO.setDuration(1500);
        requestDTO.setScore((short) 4);
        requestDTO.setNotes("Good session");

        // When
        PomodoroSession entity = mapper.toEntity(requestDTO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // ID should not be set from DTO
        assertThat(entity.getUserId()).isNull(); // userId should not be set from DTO
        assertThat(entity.getStartTime()).isEqualTo(FIXED_NOW);
        assertThat(entity.getEndTime()).isEqualTo(FIXED_NOW.plusSeconds(1500));
        assertThat(entity.getDuration()).isEqualTo(1500);
        assertThat(entity.getScore()).isEqualTo((short) 4);
        assertThat(entity.getNotes()).isEqualTo("Good session");
    }

    @Test
    void toResponseDTO_ShouldConvertEntityToResponseDTO() {
        // Given
        PomodoroSession entity = new PomodoroSession();
        entity.setId(1L);
        entity.setStartTime(FIXED_NOW);
        entity.setEndTime(FIXED_NOW.plusSeconds(1500));
        entity.setDuration(1500);
        entity.setScore((short) 5);
        entity.setNotes("Excellent focus");
        entity.setUserId(TEST_USER_ID);

        // When
        PomodoroSessionResponseDTO responseDTO = mapper.toResponseDTO(entity);

        // Then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(1L);
        assertThat(responseDTO.getStartTime()).isEqualTo(FIXED_NOW);
        assertThat(responseDTO.getEndTime()).isEqualTo(FIXED_NOW.plusSeconds(1500));
        assertThat(responseDTO.getDuration()).isEqualTo(1500);
        assertThat(responseDTO.getScore()).isEqualTo((short) 5);
        assertThat(responseDTO.getNotes()).isEqualTo("Excellent focus");
        // userId should NOT be in response DTO
    }

    @Test
    void toResponseDTOList_ShouldConvertListOfEntities() {
        // Given
        PomodoroSession entity1 = new PomodoroSession();
        entity1.setId(1L);
        entity1.setStartTime(FIXED_NOW);
        entity1.setDuration(1500);
        entity1.setUserId(TEST_USER_ID);

        PomodoroSession entity2 = new PomodoroSession();
        entity2.setId(2L);
        entity2.setStartTime(FIXED_NOW.plusSeconds(3600));
        entity2.setDuration(1500);
        entity2.setUserId(TEST_USER_ID);

        List<PomodoroSession> entities = List.of(entity1, entity2);

        // When
        List<PomodoroSessionResponseDTO> responseDTOs = mapper.toResponseDTOList(entities);

        // Then
        assertThat(responseDTOs).hasSize(2);
        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void updateEntityFromDTO_ShouldUpdateExistingEntity() {
        // Given
        PomodoroSession existingEntity = new PomodoroSession();
        existingEntity.setId(1L);
        existingEntity.setStartTime(FIXED_NOW);
        existingEntity.setEndTime(FIXED_NOW.plusSeconds(1000));
        existingEntity.setDuration(1000);
        existingEntity.setScore((short) 3);
        existingEntity.setNotes("Old notes");
        existingEntity.setUserId(TEST_USER_ID);

        PomodoroSessionRequestDTO requestDTO = new PomodoroSessionRequestDTO();
        requestDTO.setStartTime(FIXED_NOW.plusSeconds(100));
        requestDTO.setEndTime(FIXED_NOW.plusSeconds(1600));
        requestDTO.setDuration(1500);
        requestDTO.setScore((short) 5);
        requestDTO.setNotes("Updated notes");

        // When
        mapper.updateEntityFromDTO(existingEntity, requestDTO);

        // Then
        assertThat(existingEntity.getId()).isEqualTo(1L); // ID should not change
        assertThat(existingEntity.getUserId()).isEqualTo(TEST_USER_ID); // userId should not change
        assertThat(existingEntity.getStartTime()).isEqualTo(FIXED_NOW.plusSeconds(100));
        assertThat(existingEntity.getEndTime()).isEqualTo(FIXED_NOW.plusSeconds(1600));
        assertThat(existingEntity.getDuration()).isEqualTo(1500);
        assertThat(existingEntity.getScore()).isEqualTo((short) 5);
        assertThat(existingEntity.getNotes()).isEqualTo("Updated notes");
    }

    @Test
    void toEntity_WithNullDTO_ShouldReturnNull() {
        // When
        PomodoroSession entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    void toResponseDTO_WithNullEntity_ShouldReturnNull() {
        // When
        PomodoroSessionResponseDTO responseDTO = mapper.toResponseDTO(null);

        // Then
        assertThat(responseDTO).isNull();
    }
}

