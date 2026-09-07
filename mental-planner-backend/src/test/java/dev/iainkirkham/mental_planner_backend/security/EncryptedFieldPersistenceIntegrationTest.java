package dev.iainkirkham.mental_planner_backend.security;

import dev.iainkirkham.mental_planner_backend.config.TestAuthenticationConfig;
import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import dev.iainkirkham.mental_planner_backend.mood.MoodEntry;
import dev.iainkirkham.mental_planner_backend.mood.MoodEntryRepository;
import dev.iainkirkham.mental_planner_backend.subtasks.Subtask;
import dev.iainkirkham.mental_planner_backend.subtasks.SubtaskRepository;
import dev.iainkirkham.mental_planner_backend.tasks.Task;
import dev.iainkirkham.mental_planner_backend.tasks.TaskRepository;
import dev.iainkirkham.mental_planner_backend.timeentries.TaskTimeEntry;
import dev.iainkirkham.mental_planner_backend.timeentries.TaskTimeEntryRepository;
import dev.iainkirkham.mental_planner_backend.timeentries.TimeEntrySource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the fields wired up to {@link EncryptedStringConverter} /
 * {@link EncryptedStringListConverter} are genuinely stored as ciphertext in Postgres
 * (read via raw JDBC, bypassing Hibernate) while still round-tripping correctly through
 * the repository layer. See docs/security/data-at-rest-encryption-plan.md.
 */
@SpringBootTest
@Import({TestcontainersConfiguration.class, TestAuthenticationConfig.class})
@ActiveProfiles("test")
class EncryptedFieldPersistenceIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private TaskTimeEntryRepository taskTimeEntryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void task_titleDescriptionAndCategory_areStoredEncryptedAndRoundTrip() {
        Task task = new Task();
        task.setTitle("Book therapy appointment");
        task.setDescription("Call the clinic before 5pm and ask for Thursday");
        task.setCategory("personal");
        task.setScheduledDate(LocalDate.of(2026, 9, 4));
        task.setUserId(TestAuthenticationConfig.TEST_USER_ID);

        Task saved = taskRepository.save(task);

        assertRawColumnIsCiphertext("task", "title", saved.getId(), "Book therapy appointment");
        assertRawColumnIsCiphertext("task", "description", saved.getId(), "Call the clinic before 5pm and ask for Thursday");
        assertRawColumnIsCiphertext("task", "category", saved.getId(), "personal");

        Task reloaded = taskRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Book therapy appointment");
        assertThat(reloaded.getDescription()).isEqualTo("Call the clinic before 5pm and ask for Thursday");
        assertThat(reloaded.getCategory()).isEqualTo("personal");
    }

    @Test
    void subtask_title_isStoredEncryptedAndRoundTrips() {
        Task task = new Task();
        task.setTitle("Plan the week");
        task.setScheduledDate(LocalDate.of(2026, 9, 4));
        task.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        Task savedTask = taskRepository.save(task);

        Subtask subtask = new Subtask();
        subtask.setTaskId(savedTask.getId());
        subtask.setTitle("Draft the agenda");
        Subtask saved = subtaskRepository.save(subtask);

        assertRawColumnIsCiphertext("subtask", "title", saved.getId(), "Draft the agenda");

        Subtask reloaded = subtaskRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Draft the agenda");
    }

    @Test
    void moodEntry_notesAndFactors_areStoredEncryptedAndRoundTrip() {
        MoodEntry entry = new MoodEntry();
        entry.setMoodScore((short) 4);
        entry.setDateTime(Instant.parse("2026-09-04T09:00:00Z"));
        entry.setNotes("Slept badly, feeling anxious about the deadline");
        entry.setFactors(List.of("sleep", "work stress"));
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);

        MoodEntry saved = moodEntryRepository.save(entry);

        assertRawColumnIsCiphertext("mood_entry", "notes", saved.getId(), "Slept badly, feeling anxious about the deadline");
        assertRawColumnIsCiphertext("mood_entry", "factors", saved.getId(), "sleep");
        assertRawColumnIsCiphertext("mood_entry", "factors", saved.getId(), "work stress");

        MoodEntry reloaded = moodEntryRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getNotes()).isEqualTo("Slept badly, feeling anxious about the deadline");
        assertThat(reloaded.getFactors()).containsExactly("sleep", "work stress");
    }

    @Test
    void taskTimeEntry_notes_isStoredEncryptedAndRoundTrips() {
        TaskTimeEntry entry = new TaskTimeEntry();
        entry.setStartedAt(Instant.parse("2026-09-04T09:00:00Z"));
        entry.setEndedAt(Instant.parse("2026-09-04T09:25:00Z"));
        entry.setMinutes(25);
        entry.setEntryDate(LocalDate.of(2026, 9, 4));
        entry.setSource(TimeEntrySource.FOCUS);
        entry.setNotes("Struggled to focus, kept thinking about the appointment");
        entry.setUserId(TestAuthenticationConfig.TEST_USER_ID);

        TaskTimeEntry saved = taskTimeEntryRepository.save(entry);

        assertRawColumnIsCiphertext("task_time_entry", "notes", saved.getId(), "Struggled to focus, kept thinking about the appointment");

        TaskTimeEntry reloaded = taskTimeEntryRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getNotes()).isEqualTo("Struggled to focus, kept thinking about the appointment");
    }

    private void assertRawColumnIsCiphertext(String table, String column, Long id, String plaintext) {
        String raw = jdbcTemplate.queryForObject(
                "SELECT " + column + " FROM " + table + " WHERE id = ?", String.class, id);
        assertThat(raw).isNotNull();
        assertThat(raw).doesNotContain(plaintext);
    }
}
