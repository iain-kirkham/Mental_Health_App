package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TaskTimeEntryMapper.
 * Verifies DTO to entity conversions and vice versa.
 */
class TaskTimeEntryMapperTest {

    private TaskTimeEntryMapper mapper;
    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T09:00:00Z");
    private static final LocalDate FIXED_DATE = LocalDate.parse("2025-12-01");
    private static final String TEST_USER_ID = "user_test123";

    @BeforeEach
    void setUp() {
        mapper = new TaskTimeEntryMapper();
    }

    @Test
    void toEntity_ShouldConvertStopwatchRequestDTOToEntity() {
        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setStartedAt(FIXED_NOW);
        requestDTO.setEndedAt(FIXED_NOW.plusSeconds(1800));
        requestDTO.setMinutes(30);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.STOPWATCH);

        TaskTimeEntry entity = mapper.toEntity(requestDTO, 1L);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getUserId()).isNull(); // userId should not be set from DTO
        assertThat(entity.getTaskId()).isEqualTo(1L);
        assertThat(entity.getStartedAt()).isEqualTo(FIXED_NOW);
        assertThat(entity.getEndedAt()).isEqualTo(FIXED_NOW.plusSeconds(1800));
        assertThat(entity.getMinutes()).isEqualTo(30);
        assertThat(entity.getEntryDate()).isEqualTo(FIXED_DATE);
        assertThat(entity.getSource()).isEqualTo(TimeEntrySource.STOPWATCH);
    }

    @Test
    void toEntity_ShouldConvertCountdownRequestDTOToEntity() {
        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setStartedAt(FIXED_NOW);
        requestDTO.setEndedAt(FIXED_NOW.plusSeconds(1500));
        requestDTO.setMinutes(25);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.COUNTDOWN);

        TaskTimeEntry entity = mapper.toEntity(requestDTO, 1L);

        assertThat(entity).isNotNull();
        assertThat(entity.getTaskId()).isEqualTo(1L);
        assertThat(entity.getStartedAt()).isEqualTo(FIXED_NOW);
        assertThat(entity.getEndedAt()).isEqualTo(FIXED_NOW.plusSeconds(1500));
        assertThat(entity.getMinutes()).isEqualTo(25);
        assertThat(entity.getEntryDate()).isEqualTo(FIXED_DATE);
        assertThat(entity.getSource()).isEqualTo(TimeEntrySource.COUNTDOWN);
    }

    @Test
    void toEntity_ShouldConvertManualRequestDTOWithNoTimestamps() {
        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setMinutes(45);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.MANUAL);
        requestDTO.setNote("Logged after the fact");

        TaskTimeEntry entity = mapper.toEntity(requestDTO, 2L);

        assertThat(entity.getTaskId()).isEqualTo(2L);
        assertThat(entity.getStartedAt()).isNull();
        assertThat(entity.getEndedAt()).isNull();
        assertThat(entity.getMinutes()).isEqualTo(45);
        assertThat(entity.getSource()).isEqualTo(TimeEntrySource.MANUAL);
        assertThat(entity.getNote()).isEqualTo("Logged after the fact");
    }

    @Test
    void toResponseDTO_ShouldConvertEntityToResponseDTO() {
        TaskTimeEntry entity = new TaskTimeEntry();
        entity.setId(1L);
        entity.setTaskId(5L);
        entity.setUserId(TEST_USER_ID);
        entity.setStartedAt(FIXED_NOW);
        entity.setEndedAt(FIXED_NOW.plusSeconds(600));
        entity.setMinutes(10);
        entity.setEntryDate(FIXED_DATE);
        entity.setSource(TimeEntrySource.STOPWATCH);
        entity.setNote("Focused burst");

        TaskTimeEntryResponseDTO responseDTO = mapper.toResponseDTO(entity);

        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(1L);
        assertThat(responseDTO.getTaskId()).isEqualTo(5L);
        assertThat(responseDTO.getStartedAt()).isEqualTo(FIXED_NOW);
        assertThat(responseDTO.getEndedAt()).isEqualTo(FIXED_NOW.plusSeconds(600));
        assertThat(responseDTO.getMinutes()).isEqualTo(10);
        assertThat(responseDTO.getEntryDate()).isEqualTo(FIXED_DATE);
        assertThat(responseDTO.getSource()).isEqualTo(TimeEntrySource.STOPWATCH);
        assertThat(responseDTO.getNote()).isEqualTo("Focused burst");
        // userId should NOT be in response DTO
    }

    @Test
    void toResponseDTOList_ShouldConvertListOfEntities() {
        TaskTimeEntry entry1 = new TaskTimeEntry();
        entry1.setId(1L);
        entry1.setTaskId(5L);
        entry1.setMinutes(10);
        entry1.setEntryDate(FIXED_DATE);
        entry1.setSource(TimeEntrySource.STOPWATCH);

        TaskTimeEntry entry2 = new TaskTimeEntry();
        entry2.setId(2L);
        entry2.setTaskId(5L);
        entry2.setMinutes(20);
        entry2.setEntryDate(FIXED_DATE.minusDays(1));
        entry2.setSource(TimeEntrySource.MANUAL);

        List<TaskTimeEntryResponseDTO> responseDTOs = mapper.toResponseDTOList(List.of(entry1, entry2));

        assertThat(responseDTOs).hasSize(2);
        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void toEntity_WithNullDTO_ShouldReturnNull() {
        assertThat(mapper.toEntity(null, 1L)).isNull();
    }

    @Test
    void toResponseDTO_WithNullEntity_ShouldReturnNull() {
        assertThat(mapper.toResponseDTO(null)).isNull();
    }
}
