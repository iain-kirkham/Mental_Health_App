package dev.iainkirkham.mental_planner_backend.tasks;

import dev.iainkirkham.mental_planner_backend.config.TestAuthenticationConfig;
import dev.iainkirkham.mental_planner_backend.config.TestSecurityConfiguration;
import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import dev.iainkirkham.mental_planner_backend.tasks.dto.CompletionRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskReorderItemDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskResponseDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskTimeEntryResponseDTO;
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

    @Autowired
    private TaskTimeEntryRepository taskTimeEntryRepository;

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
        taskTimeEntryRepository.deleteAll();
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

    // --- time entries: stopwatch entries are history-only, manual entries drive actualMinutes ---

    @Test
    void logTimeEntry_StopwatchEntry_ShouldNotChangeActualMinutes() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(25);
        taskRepository.save(task);

        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setStartedAt(java.time.Instant.parse("2025-12-01T09:00:00Z"));
        requestDTO.setEndedAt(java.time.Instant.parse("2025-12-01T09:30:00Z"));
        requestDTO.setMinutes(30);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.STOPWATCH);

        ResponseEntity<TaskTimeEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/tasks/" + task.getId() + "/time-entries",
                requestDTO,
                TaskTimeEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSource()).isEqualTo(TimeEntrySource.STOPWATCH);
        // actualMinutes is already kept correct by the stopwatch's own persist path - logging
        // the entry must not double-count it.
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(25);
    }

    @Test
    void logTimeEntry_CountdownEntry_ShouldNotChangeActualMinutes() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(25);
        taskRepository.save(task);

        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setStartedAt(java.time.Instant.parse("2025-12-01T09:00:00Z"));
        requestDTO.setEndedAt(java.time.Instant.parse("2025-12-01T09:25:00Z"));
        requestDTO.setMinutes(25);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.COUNTDOWN);

        ResponseEntity<TaskTimeEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/tasks/" + task.getId() + "/time-entries",
                requestDTO,
                TaskTimeEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSource()).isEqualTo(TimeEntrySource.COUNTDOWN);
        // Like STOPWATCH, a completed focus session already has actualMinutes kept correct by
        // its own persist path - logging the entry must not double-count it.
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(25);
    }

    @Test
    void logTimeEntry_ManualEntry_ShouldAddToActualMinutes() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(10);
        taskRepository.save(task);

        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setMinutes(15);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.MANUAL);
        requestDTO.setNote("Forgot to start the timer");

        ResponseEntity<TaskTimeEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/tasks/" + task.getId() + "/time-entries",
                requestDTO,
                TaskTimeEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(25);
    }

    @Test
    void deleteTimeEntry_ManualEntry_ShouldSubtractFromActualMinutes() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(20);
        taskRepository.save(task);

        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setTaskId(task.getId());
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        entry.setMinutes(15);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.MANUAL);
        entry = taskTimeEntryRepository.save(entry);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/" + task.getId() + "/time-entries/" + entry.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(5);
        assertThat(taskTimeEntryRepository.findById(entry.getId())).isEmpty();
    }

    @Test
    void deleteTimeEntry_StopwatchEntry_ShouldLeaveActualMinutesUnchanged() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(30);
        taskRepository.save(task);

        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setTaskId(task.getId());
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        entry.setMinutes(30);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.STOPWATCH);
        entry = taskTimeEntryRepository.save(entry);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/" + task.getId() + "/time-entries/" + entry.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(30);
    }

    @Test
    void deleteTimeEntry_CountdownEntry_ShouldLeaveActualMinutesUnchanged() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(25);
        taskRepository.save(task);

        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setTaskId(task.getId());
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        entry.setMinutes(25);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.COUNTDOWN);
        entry = taskTimeEntryRepository.save(entry);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/" + task.getId() + "/time-entries/" + entry.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(25);
    }

    @Test
    void getTimeEntries_ShouldReturnEntriesMostRecentDayFirst() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);

        TaskTimeEntry older = new TaskTimeEntry();
        older.setTaskId(task.getId());
        older.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        older.setMinutes(10);
        older.setEntryDate(FIXED_DATE.minusDays(1));
        older.setSource(TimeEntrySource.MANUAL);
        taskTimeEntryRepository.save(older);

        TaskTimeEntry newer = new TaskTimeEntry();
        newer.setTaskId(task.getId());
        newer.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        newer.setMinutes(20);
        newer.setEntryDate(FIXED_DATE);
        newer.setSource(TimeEntrySource.MANUAL);
        taskTimeEntryRepository.save(newer);

        ResponseEntity<List<TaskTimeEntryResponseDTO>> response = restTemplate.exchange(
                "/api/tasks/" + task.getId() + "/time-entries",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(TaskTimeEntryResponseDTO::getEntryDate)
                .containsExactly(FIXED_DATE, FIXED_DATE.minusDays(1));
    }

    @Test
    void timeEntries_ShouldReturnNotFoundForAnotherUsersTask() {
        Task othersTask = createTaskInDb("Not mine", OTHER_USER_ID);

        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setMinutes(10);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.MANUAL);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/tasks/" + othersTask.getId() + "/time-entries",
                requestDTO,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
