package dev.iainkirkham.mental_planner_backend.security;

import dev.iainkirkham.mental_planner_backend.config.TestAuthenticationConfig;
import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import dev.iainkirkham.mental_planner_backend.mood.MoodEntry;
import dev.iainkirkham.mental_planner_backend.mood.MoodEntryRepository;
import dev.iainkirkham.mental_planner_backend.pomodoro.PomodoroSession;
import dev.iainkirkham.mental_planner_backend.pomodoro.PomodoroSessionRepository;
import dev.iainkirkham.mental_planner_backend.tasks.Subtask;
import dev.iainkirkham.mental_planner_backend.tasks.SubtaskRepository;
import dev.iainkirkham.mental_planner_backend.tasks.Task;
import dev.iainkirkham.mental_planner_backend.tasks.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies {@link EncryptionBackfillRunner} against legacy plaintext rows inserted directly
 * via JDBC (bypassing Hibernate/the converters, simulating rows written before field
 * encryption existed). The runner already ran once during context startup against an empty
 * database - each test inserts plaintext rows afterward, then re-invokes {@code run} directly
 * to exercise the backfill against them, which incidentally also proves the job is safe to
 * run more than once. See docs/security/data-at-rest-encryption-plan.md, step 6.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = "app.encryption.backfill.enabled=true")
class EncryptionBackfillRunnerIntegrationTest {

    @Autowired
    private EncryptionBackfillRunner backfillRunner;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Test
    void backfill_encryptsLegacyPlaintextTaskAndSubtaskRowsAndTheyRoundTrip() {
        Long taskId = jdbcTemplate.queryForObject(
                "INSERT INTO task (title, description, category, scheduled_date, user_id) " +
                        "VALUES (?, ?, ?, CURRENT_DATE, ?) RETURNING id",
                Long.class,
                "Book therapy appointment", "Call before 5pm", "personal", TestAuthenticationConfig.TEST_USER_ID);
        Long subtaskId = jdbcTemplate.queryForObject(
                "INSERT INTO subtask (task_id, title) VALUES (?, ?) RETURNING id",
                Long.class, taskId, "Find the phone number");

        backfillRunner.run(new DefaultApplicationArguments());

        assertColumnIsEncrypted("task", "title", taskId);
        assertColumnIsEncrypted("task", "description", taskId);
        assertColumnIsEncrypted("task", "category", taskId);
        assertColumnIsEncrypted("subtask", "title", subtaskId);

        Task task = taskRepository.findById(taskId).orElseThrow();
        assertThat(task.getTitle()).isEqualTo("Book therapy appointment");
        assertThat(task.getDescription()).isEqualTo("Call before 5pm");
        assertThat(task.getCategory()).isEqualTo("personal");

        Subtask subtask = subtaskRepository.findById(subtaskId).orElseThrow();
        assertThat(subtask.getTitle()).isEqualTo("Find the phone number");
    }

    @Test
    void backfill_encryptsLegacyMoodEntryNotesAndJsonFactorsAndTheyRoundTrip() {
        Long moodEntryId = jdbcTemplate.queryForObject(
                "INSERT INTO mood_entry (mood_score, date_time, notes, factors, user_id) " +
                        "VALUES (4, now(), ?, ?, ?) RETURNING id",
                Long.class,
                "Slept badly, anxious about the deadline", "[\"sleep\", \"work stress\"]",
                TestAuthenticationConfig.TEST_USER_ID);

        backfillRunner.run(new DefaultApplicationArguments());

        assertColumnIsEncrypted("mood_entry", "notes", moodEntryId);
        assertColumnIsEncrypted("mood_entry", "factors", moodEntryId);

        MoodEntry reloaded = moodEntryRepository.findById(moodEntryId).orElseThrow();
        assertThat(reloaded.getNotes()).isEqualTo("Slept badly, anxious about the deadline");
        assertThat(reloaded.getFactors()).containsExactly("sleep", "work stress");
    }

    @Test
    void backfill_encryptsLegacyPomodoroSessionNotesAndTheyRoundTrip() {
        Long sessionId = jdbcTemplate.queryForObject(
                "INSERT INTO pomodoro_session (start_time, end_time, duration, notes, user_id) " +
                        "VALUES (now(), now(), 25, ?, ?) RETURNING id",
                Long.class, "Struggled to focus", TestAuthenticationConfig.TEST_USER_ID);

        backfillRunner.run(new DefaultApplicationArguments());

        assertColumnIsEncrypted("pomodoro_session", "notes", sessionId);

        PomodoroSession reloaded = pomodoroSessionRepository.findById(sessionId).orElseThrow();
        assertThat(reloaded.getNotes()).isEqualTo("Struggled to focus");
    }

    @Test
    void backfill_leavesAlreadyEncryptedRowsUntouchedOnRepeatedRuns() {
        Task task = new Task();
        task.setTitle("Already encrypted title");
        task.setScheduledDate(java.time.LocalDate.now());
        task.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        Long taskId = taskRepository.save(task).getId();

        String before = jdbcTemplate.queryForObject(
                "SELECT title FROM task WHERE id = ?", String.class, taskId);

        backfillRunner.run(new DefaultApplicationArguments());
        backfillRunner.run(new DefaultApplicationArguments());

        String after = jdbcTemplate.queryForObject(
                "SELECT title FROM task WHERE id = ?", String.class, taskId);
        assertThat(after).isEqualTo(before);
    }

    private void assertColumnIsEncrypted(String table, String column, Long id) {
        String raw = jdbcTemplate.queryForObject(
                "SELECT " + column + " FROM " + table + " WHERE id = ?", String.class, id);
        assertThat(raw).startsWith(EncryptionService.CIPHERTEXT_PREFIX);
    }
}
