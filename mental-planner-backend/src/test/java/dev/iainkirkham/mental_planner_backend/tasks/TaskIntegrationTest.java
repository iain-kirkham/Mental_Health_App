package dev.iainkirkham.mental_planner_backend.tasks;

import dev.iainkirkham.mental_planner_backend.config.TestAuthenticationConfig;
import dev.iainkirkham.mental_planner_backend.config.TestSecurityConfiguration;
import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import dev.iainkirkham.mental_planner_backend.subtasks.Subtask;
import dev.iainkirkham.mental_planner_backend.subtasks.SubtaskRepository;
import dev.iainkirkham.mental_planner_backend.tasks.dto.CompletionRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskReorderItemDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Task API, focused on the stateful batch operations
 * (reorder, completion cascade) that carry the most risk of a partial or
 * inconsistent write.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, TestAuthenticationConfig.class, TestSecurityConfiguration.class})
@org.springframework.test.context.ActiveProfiles("test")
class TaskIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    private static final LocalDate FIXED_DATE = LocalDate.parse("2025-12-01");
    private static final String OTHER_USER_ID = "user_someone_else";

    private Task createTaskInDb(String title, String userId) {
        Task task = new Task();
        task.setTitle(title);
        task.setScheduledDate(FIXED_DATE);
        task.setUserId(userId);
        return taskRepository.save(task);
    }

    private Subtask createSubtaskInDb(Long taskId, String title, boolean completed) {
        Subtask subtask = new Subtask();
        subtask.setTaskId(taskId);
        subtask.setTitle(title);
        subtask.setCompleted(completed);
        return subtaskRepository.save(subtask);
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        subtaskRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateTask() {
        TaskRequestDTO newTask = new TaskRequestDTO();
        newTask.setTitle("Write report");
        newTask.setScheduledDate(FIXED_DATE);
        newTask.setPriority(TaskPriority.NORMAL);

        ResponseEntity<TaskResponseDTO> response = restTemplate.postForEntity("/api/tasks", newTask, TaskResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(created -> {
            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotNull();
            assertThat(created.getTitle()).isEqualTo("Write report");
            assertThat(created.getSubtasks()).isEmpty();
        });
    }

    @Test
    void shouldReturnNotFoundForNonExistentTask() {
        ResponseEntity<Void> response = restTemplate.getForEntity("/api/tasks/999", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- reorderTasks: the risky batch operation ---

    @Test
    void reorderTasks_ShouldApplyNewSortOrderToAllTasks() {
        Task first = createTaskInDb("First", TestAuthenticationConfig.TEST_USER_ID);
        Task second = createTaskInDb("Second", TestAuthenticationConfig.TEST_USER_ID);

        List<TaskReorderItemDTO> items = List.of(
                new TaskReorderItemDTO(first.getId(), 1),
                new TaskReorderItemDTO(second.getId(), 0)
        );

        ResponseEntity<List<TaskResponseDTO>> response = restTemplate.exchange(
                "/api/tasks/reorder",
                HttpMethod.PUT,
                new HttpEntity<>(items),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Task firstReloaded = taskRepository.findById(first.getId()).orElseThrow();
        Task secondReloaded = taskRepository.findById(second.getId()).orElseThrow();
        assertThat(firstReloaded.getSortOrder()).isEqualTo(1);
        assertThat(secondReloaded.getSortOrder()).isEqualTo(0);
    }

    @Test
    void reorderTasks_ShouldRejectBatchContainingAnotherUsersTaskAndPersistNothing() {
        Task owned = createTaskInDb("Mine", TestAuthenticationConfig.TEST_USER_ID);
        Task othersTask = createTaskInDb("Not mine", OTHER_USER_ID);
        int ownedOriginalSortOrder = owned.getSortOrder();

        List<TaskReorderItemDTO> items = List.of(
                new TaskReorderItemDTO(owned.getId(), 5),
                new TaskReorderItemDTO(othersTask.getId(), 0)
        );

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/reorder",
                HttpMethod.PUT,
                new HttpEntity<>(items),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        // Nothing should have been persisted, including the caller's own task earlier in the batch
        Task ownedReloaded = taskRepository.findById(owned.getId()).orElseThrow();
        assertThat(ownedReloaded.getSortOrder()).isEqualTo(ownedOriginalSortOrder);
    }

    // --- setCompletionCascade: parent + subtask fan-out, atomically ---

    @Test
    void setCompletionCascade_ShouldMarkParentAndAllSubtasksComplete() {
        Task parent = createTaskInDb("Parent", TestAuthenticationConfig.TEST_USER_ID);
        Subtask subtaskA = createSubtaskInDb(parent.getId(), "Subtask A", false);
        Subtask subtaskB = createSubtaskInDb(parent.getId(), "Subtask B", false);

        CompletionRequestDTO requestDTO = new CompletionRequestDTO();
        requestDTO.setCompleted(true);

        ResponseEntity<TaskResponseDTO> response = restTemplate.exchange(
                "/api/tasks/" + parent.getId() + "/completion",
                HttpMethod.PUT,
                new HttpEntity<>(requestDTO),
                TaskResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isCompleted()).isTrue();
        assertThat(response.getBody().getSubtasks())
                .hasSize(2)
                .allSatisfy(subtask -> assertThat(subtask.isCompleted()).isTrue());

        // Verify persisted, not just returned in the response
        assertThat(taskRepository.findById(parent.getId()).orElseThrow().isCompleted()).isTrue();
        assertThat(subtaskRepository.findByTaskIdOrderBySortOrderAsc(parent.getId()))
                .extracting(Subtask::getId, Subtask::isCompleted)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(subtaskA.getId(), true),
                        org.assertj.core.groups.Tuple.tuple(subtaskB.getId(), true)
                );
    }

    @Test
    void setCompletionCascade_ShouldReturnNotFoundForAnotherUsersTask() {
        Task othersTask = createTaskInDb("Not mine", OTHER_USER_ID);

        CompletionRequestDTO requestDTO = new CompletionRequestDTO();
        requestDTO.setCompleted(true);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/" + othersTask.getId() + "/completion",
                HttpMethod.PUT,
                new HttpEntity<>(requestDTO),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteTask_ShouldReturnNotFoundForAnotherUsersTask() {
        Task othersTask = createTaskInDb("Not mine", OTHER_USER_ID);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/" + othersTask.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(taskRepository.findById(othersTask.getId())).isPresent();
    }
}
