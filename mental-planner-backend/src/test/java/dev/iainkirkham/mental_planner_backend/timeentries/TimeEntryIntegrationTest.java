package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.config.TestAuthenticationConfig;
import dev.iainkirkham.mental_planner_backend.config.TestSecurityConfiguration;
import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import dev.iainkirkham.mental_planner_backend.tasks.Task;
import dev.iainkirkham.mental_planner_backend.tasks.TaskRepository;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TimeEntryReflectionRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the time-entry API: stopwatch/focus entries are history-only,
 * manual entries drive actualMinutes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, TestAuthenticationConfig.class, TestSecurityConfiguration.class})
@org.springframework.test.context.ActiveProfiles("test")
class TimeEntryIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

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

    @BeforeEach
    @AfterEach
    void cleanUp() {
        taskTimeEntryRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void logTimeEntry_StopwatchEntry_ShouldNotChangeActualMinutes() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(25);
        taskRepository.save(task);

        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setTaskId(task.getId());
        requestDTO.setStartedAt(java.time.Instant.parse("2025-12-01T09:00:00Z"));
        requestDTO.setEndedAt(java.time.Instant.parse("2025-12-01T09:30:00Z"));
        requestDTO.setMinutes(30);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.STOPWATCH);

        ResponseEntity<TaskTimeEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/time-entries",
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
    void logTimeEntry_FocusEntry_ShouldNotChangeActualMinutes() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(25);
        taskRepository.save(task);

        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setTaskId(task.getId());
        requestDTO.setStartedAt(java.time.Instant.parse("2025-12-01T09:00:00Z"));
        requestDTO.setEndedAt(java.time.Instant.parse("2025-12-01T09:25:00Z"));
        requestDTO.setMinutes(25);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.FOCUS);

        ResponseEntity<TaskTimeEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/time-entries",
                requestDTO,
                TaskTimeEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSource()).isEqualTo(TimeEntrySource.FOCUS);
        // Like STOPWATCH, a completed focus session already has actualMinutes kept correct by
        // its own persist path - logging the entry must not double-count it.
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(25);
    }

    @Test
    void logTimeEntry_TaskLessFocusEntry_ShouldBeAccepted() {
        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setStartedAt(java.time.Instant.parse("2025-12-01T09:00:00Z"));
        requestDTO.setEndedAt(java.time.Instant.parse("2025-12-01T09:25:00Z"));
        requestDTO.setMinutes(25);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.FOCUS);

        ResponseEntity<TaskTimeEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/time-entries",
                requestDTO,
                TaskTimeEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTaskId()).isNull();
    }

    @Test
    void logTimeEntry_ManualEntryWithNoTaskId_ShouldBeRejected() {
        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setMinutes(15);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.MANUAL);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/time-entries",
                requestDTO,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void logTimeEntry_ManualEntry_ShouldAddToActualMinutes() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(10);
        taskRepository.save(task);

        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setTaskId(task.getId());
        requestDTO.setMinutes(15);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.MANUAL);
        requestDTO.setNotes("Forgot to start the timer");

        ResponseEntity<TaskTimeEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/time-entries",
                requestDTO,
                TaskTimeEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(25);
    }

    @Test
    void updateTimeEntryReflection_ShouldAttachScoreEnergyAndNotes() {
        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        entry.setMinutes(25);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.FOCUS);
        entry = taskTimeEntryRepository.save(entry);

        TimeEntryReflectionRequestDTO requestDTO = new TimeEntryReflectionRequestDTO();
        requestDTO.setScore((short) 4);
        requestDTO.setEnergyRating(EnergyRating.ENERGIZING);
        requestDTO.setNotes("Focused well");

        ResponseEntity<TaskTimeEntryResponseDTO> response = restTemplate.exchange(
                "/api/time-entries/" + entry.getId(),
                HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(requestDTO),
                TaskTimeEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getScore()).isEqualTo((short) 4);
        assertThat(response.getBody().getEnergyRating()).isEqualTo(EnergyRating.ENERGIZING);
        assertThat(response.getBody().getNotes()).isEqualTo("Focused well");
        assertThat(response.getBody().getMinutes()).isEqualTo(25);
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
                "/api/time-entries/" + entry.getId(),
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
                "/api/time-entries/" + entry.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(30);
    }

    @Test
    void deleteTimeEntry_FocusEntry_ShouldLeaveActualMinutesUnchanged() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);
        task.recordTimerCheckpoint(25);
        taskRepository.save(task);

        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setTaskId(task.getId());
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        entry.setMinutes(25);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.FOCUS);
        entry = taskTimeEntryRepository.save(entry);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/time-entries/" + entry.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getActualMinutes()).isEqualTo(25);
    }

    @Test
    void deleteTimeEntry_TaskLessEntry_ShouldSucceed() {
        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        entry.setMinutes(25);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.FOCUS);
        entry = taskTimeEntryRepository.save(entry);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/time-entries/" + entry.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskTimeEntryRepository.findById(entry.getId())).isEmpty();
    }

    @Test
    void deletingATask_ShouldOrphanItsTimeEntriesRatherThanDeletingThem() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);

        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setTaskId(task.getId());
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        entry.setMinutes(25);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.FOCUS);
        entry = taskTimeEntryRepository.save(entry);

        taskRepository.delete(task);
        taskRepository.flush();

        TaskTimeEntry reloaded = taskTimeEntryRepository.findById(entry.getId()).orElseThrow();
        assertThat(reloaded.getTaskId()).isNull();
    }

    @Test
    void getTimeEntriesForTask_ShouldReturnEntriesMostRecentDayFirst() {
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
    void getTimeEntries_ShouldReturnEntriesAcrossTasksWithinDateRange() {
        Task task = createTaskInDb("Deep work", TestAuthenticationConfig.TEST_USER_ID);

        TaskTimeEntry linked = new TaskTimeEntry();
        linked.setTaskId(task.getId());
        linked.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        linked.setMinutes(10);
        linked.setEntryDate(FIXED_DATE);
        linked.setSource(TimeEntrySource.MANUAL);
        taskTimeEntryRepository.save(linked);

        TaskTimeEntry taskLess = new TaskTimeEntry();
        taskLess.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        taskLess.setMinutes(25);
        taskLess.setEntryDate(FIXED_DATE);
        taskLess.setSource(TimeEntrySource.FOCUS);
        taskTimeEntryRepository.save(taskLess);

        TaskTimeEntry outOfRange = new TaskTimeEntry();
        outOfRange.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        outOfRange.setMinutes(5);
        outOfRange.setEntryDate(FIXED_DATE.minusDays(30));
        outOfRange.setSource(TimeEntrySource.FOCUS);
        taskTimeEntryRepository.save(outOfRange);

        ResponseEntity<List<TaskTimeEntryResponseDTO>> response = restTemplate.exchange(
                "/api/time-entries?from=" + FIXED_DATE + "&to=" + FIXED_DATE,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(TaskTimeEntryResponseDTO::getMinutes)
                .containsExactlyInAnyOrder(10, 25);
    }

    @Test
    void timeEntries_ShouldReturnNotFoundForAnotherUsersTask() {
        Task othersTask = createTaskInDb("Not mine", OTHER_USER_ID);

        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setTaskId(othersTask.getId());
        requestDTO.setMinutes(10);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.MANUAL);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/time-entries",
                requestDTO,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
