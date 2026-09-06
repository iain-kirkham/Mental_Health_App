package dev.iainkirkham.mental_planner_backend.tasks;

import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TaskMapper.
 * Verifies DTO to entity conversions and vice versa.
 */
class TaskMapperTest {

    private TaskMapper mapper;
    private static final LocalDate FIXED_DATE = LocalDate.parse("2025-12-01");
    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T09:00:00Z");
    private static final String TEST_USER_ID = "user_test123";

    @BeforeEach
    void setUp() {
        mapper = new TaskMapper();
    }

    @Test
    void toEntity_ShouldConvertRequestDTOToEntity() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Write report");
        requestDTO.setDescription("Quarterly summary");
        requestDTO.setScheduledDate(FIXED_DATE);
        requestDTO.setStartTime(FIXED_NOW);
        requestDTO.setEndTime(FIXED_NOW.plusSeconds(3600));
        requestDTO.setCompleted(false);
        requestDTO.setSortOrder(2);
        requestDTO.setPlannedMinutes(60);
        requestDTO.setActualMinutes(15);
        requestDTO.setCategory("planning");
        requestDTO.setArchived(false);
        requestDTO.setPriority(TaskPriority.HIGH);

        Task entity = mapper.toEntity(requestDTO);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getUserId()).isNull();
        assertThat(entity.getTitle()).isEqualTo("Write report");
        assertThat(entity.getDescription()).isEqualTo("Quarterly summary");
        assertThat(entity.getScheduledDate()).isEqualTo(FIXED_DATE);
        assertThat(entity.getStartTime()).isEqualTo(FIXED_NOW);
        assertThat(entity.getEndTime()).isEqualTo(FIXED_NOW.plusSeconds(3600));
        assertThat(entity.isCompleted()).isFalse();
        assertThat(entity.getSortOrder()).isEqualTo(2);
        assertThat(entity.getPlannedMinutes()).isEqualTo(60);
        assertThat(entity.getActualMinutes()).isEqualTo(15);
        assertThat(entity.getCategory()).isEqualTo("planning");
        assertThat(entity.isArchived()).isFalse();
        assertThat(entity.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void toEntity_WithNullPriority_ShouldDefaultToNormal() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Untitled");
        requestDTO.setScheduledDate(FIXED_DATE);
        requestDTO.setPriority(null);

        Task entity = mapper.toEntity(requestDTO);

        assertThat(entity.getPriority()).isEqualTo(TaskPriority.NORMAL);
    }

    @Test
    void toResponseDTO_ShouldConvertEntityToResponseDTO() {
        Task entity = new Task();
        entity.setId(1L);
        entity.setTitle("Write report");
        entity.setDescription("Quarterly summary");
        entity.setScheduledDate(FIXED_DATE);
        entity.setStartTime(FIXED_NOW);
        entity.setEndTime(FIXED_NOW.plusSeconds(3600));
        entity.setCompleted(true);
        entity.setSortOrder(3);
        entity.setPlannedMinutes(45);
        entity.recordTimerCheckpoint(50);
        entity.setCategory("product");
        entity.setArchived(true);
        entity.setPriority(TaskPriority.URGENT);
        entity.setUserId(TEST_USER_ID);

        TaskResponseDTO responseDTO = mapper.toResponseDTO(entity);

        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(1L);
        assertThat(responseDTO.getTitle()).isEqualTo("Write report");
        assertThat(responseDTO.getDescription()).isEqualTo("Quarterly summary");
        assertThat(responseDTO.getScheduledDate()).isEqualTo(FIXED_DATE);
        assertThat(responseDTO.getStartTime()).isEqualTo(FIXED_NOW);
        assertThat(responseDTO.getEndTime()).isEqualTo(FIXED_NOW.plusSeconds(3600));
        assertThat(responseDTO.isCompleted()).isTrue();
        assertThat(responseDTO.getSortOrder()).isEqualTo(3);
        assertThat(responseDTO.getPlannedMinutes()).isEqualTo(45);
        assertThat(responseDTO.getActualMinutes()).isEqualTo(50);
        assertThat(responseDTO.getCategory()).isEqualTo("product");
        assertThat(responseDTO.isArchived()).isTrue();
        assertThat(responseDTO.getPriority()).isEqualTo(TaskPriority.URGENT);
        // userId should NOT be in response DTO; subtasks are attached separately by the service
    }

    @Test
    void toResponseDTOList_ShouldConvertListOfEntities() {
        Task entity1 = new Task();
        entity1.setId(1L);
        entity1.setTitle("Task one");
        entity1.setScheduledDate(FIXED_DATE);
        entity1.setUserId(TEST_USER_ID);

        Task entity2 = new Task();
        entity2.setId(2L);
        entity2.setTitle("Task two");
        entity2.setScheduledDate(FIXED_DATE);
        entity2.setUserId(TEST_USER_ID);

        List<TaskResponseDTO> responseDTOs = mapper.toResponseDTOList(List.of(entity1, entity2));

        assertThat(responseDTOs).hasSize(2);
        assertThat(responseDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(responseDTOs.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void updateEntityFromDTO_ShouldUpdateExistingEntity() {
        Task existingEntity = new Task();
        existingEntity.setId(1L);
        existingEntity.setTitle("Old title");
        existingEntity.setScheduledDate(FIXED_DATE);
        existingEntity.setPriority(TaskPriority.LOW);
        existingEntity.setUserId(TEST_USER_ID);

        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("New title");
        requestDTO.setScheduledDate(FIXED_DATE.plusDays(1));
        requestDTO.setCompleted(true);
        requestDTO.setSortOrder(5);
        requestDTO.setActualMinutes(20);
        requestDTO.setPriority(TaskPriority.URGENT);

        mapper.updateEntityFromDTO(existingEntity, requestDTO);

        assertThat(existingEntity.getId()).isEqualTo(1L); // ID should not change
        assertThat(existingEntity.getUserId()).isEqualTo(TEST_USER_ID); // userId should not change
        assertThat(existingEntity.getTitle()).isEqualTo("New title");
        assertThat(existingEntity.getScheduledDate()).isEqualTo(FIXED_DATE.plusDays(1));
        assertThat(existingEntity.isCompleted()).isTrue();
        assertThat(existingEntity.getSortOrder()).isEqualTo(5);
        assertThat(existingEntity.getActualMinutes()).isEqualTo(20);
        assertThat(existingEntity.getPriority()).isEqualTo(TaskPriority.URGENT);
    }

    @Test
    void toEntity_WithNullDTO_ShouldReturnNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toResponseDTO_WithNullEntity_ShouldReturnNull() {
        assertThat(mapper.toResponseDTO(null)).isNull();
    }
}
