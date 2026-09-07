package dev.iainkirkham.mental_planner_backend.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

/**
 * One-off maintenance job that encrypts any legacy plaintext rows left over from before
 * field encryption ({@link EncryptedStringConverter} / {@link EncryptedStringListConverter})
 * was introduced. See docs/security/data-at-rest-encryption-plan.md, step 6.
 * <p>
 * Off by default and gated behind {@code app.encryption.backfill.enabled=true} - this should
 * only ever be turned on for a single deploy, then turned back off. It reads/writes columns
 * directly via {@link JdbcTemplate} (bypassing Hibernate/the converters entirely) so it can
 * tell, per value, whether it's already ciphertext via {@link EncryptionService#isEncrypted}
 * rather than going through the entity layer - which makes it safe to run more than once:
 * already-encrypted rows are left untouched.
 */
@Component
@ConditionalOnProperty(name = "app.encryption.backfill.enabled", havingValue = "true")
public class EncryptionBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EncryptionBackfillRunner.class);
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final JdbcTemplate jdbcTemplate;
    private final EncryptionService encryptionService;
    private final UserDataKeyService userDataKeyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EncryptionBackfillRunner(JdbcTemplate jdbcTemplate,
                                     EncryptionService encryptionService,
                                     UserDataKeyService userDataKeyService) {
        this.jdbcTemplate = jdbcTemplate;
        this.encryptionService = encryptionService;
        this.userDataKeyService = userDataKeyService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.warn("Starting one-off encryption backfill of legacy plaintext rows " +
                "(app.encryption.backfill.enabled=true) - remove this config var once it completes.");

        int taskRows = backfillTask();
        int subtaskRows = backfillSubtask();
        int moodEntryRows = backfillMoodEntry();
        int taskTimeEntryRows = backfillTaskTimeEntry();

        log.warn("Encryption backfill complete: task={}, subtask={}, mood_entry={}, task_time_entry={}. " +
                        "Remove app.encryption.backfill.enabled before the next deploy.",
                taskRows, subtaskRows, moodEntryRows, taskTimeEntryRows);
    }

    private int backfillTask() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, user_id, title, description, category FROM task " +
                        "WHERE NOT title LIKE 'v1:%' " +
                        "OR (description IS NOT NULL AND description NOT LIKE 'v1:%') " +
                        "OR (category IS NOT NULL AND category NOT LIKE 'v1:%')");

        for (Map<String, Object> row : rows) {
            SecretKey key = keyForRow(row, "task");
            if (key == null) {
                continue;
            }
            jdbcTemplate.update("UPDATE task SET title = ?, description = ?, category = ? WHERE id = ?",
                    encryptIfPlain((String) row.get("title"), key),
                    encryptIfPlain((String) row.get("description"), key),
                    encryptIfPlain((String) row.get("category"), key),
                    row.get("id"));
        }
        return rows.size();
    }

    private int backfillSubtask() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT s.id AS id, t.user_id AS user_id, s.title AS title " +
                        "FROM subtask s JOIN task t ON s.task_id = t.id " +
                        "WHERE NOT s.title LIKE 'v1:%'");

        for (Map<String, Object> row : rows) {
            SecretKey key = keyForRow(row, "subtask");
            if (key == null) {
                continue;
            }
            jdbcTemplate.update("UPDATE subtask SET title = ? WHERE id = ?",
                    encryptIfPlain((String) row.get("title"), key),
                    row.get("id"));
        }
        return rows.size();
    }

    private int backfillMoodEntry() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, user_id, notes, factors FROM mood_entry " +
                        "WHERE (notes IS NOT NULL AND notes NOT LIKE 'v1:%') " +
                        "OR (factors IS NOT NULL AND factors NOT LIKE 'v1:%')");

        for (Map<String, Object> row : rows) {
            SecretKey key = keyForRow(row, "mood_entry");
            if (key == null) {
                continue;
            }
            jdbcTemplate.update("UPDATE mood_entry SET notes = ?, factors = ? WHERE id = ?",
                    encryptIfPlain((String) row.get("notes"), key),
                    encryptFactorsIfPlain((String) row.get("factors"), key),
                    row.get("id"));
        }
        return rows.size();
    }

    private int backfillTaskTimeEntry() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, user_id, notes FROM task_time_entry " +
                        "WHERE notes IS NOT NULL AND notes NOT LIKE 'v1:%'");

        for (Map<String, Object> row : rows) {
            SecretKey key = keyForRow(row, "task_time_entry");
            if (key == null) {
                continue;
            }
            jdbcTemplate.update("UPDATE task_time_entry SET notes = ? WHERE id = ?",
                    encryptIfPlain((String) row.get("notes"), key),
                    row.get("id"));
        }
        return rows.size();
    }

    private SecretKey keyForRow(Map<String, Object> row, String table) {
        String userId = (String) row.get("user_id");
        if (userId == null) {
            log.warn("Skipping {} row id={} during encryption backfill - no owning user_id, " +
                    "can't derive a data key for it", table, row.get("id"));
            return null;
        }
        return userDataKeyService.getDataKey(userId);
    }

    private String encryptIfPlain(String rawValue, SecretKey key) {
        if (rawValue == null || encryptionService.isEncrypted(rawValue)) {
            return rawValue;
        }
        return encryptionService.encrypt(rawValue, key);
    }

    /**
     * {@code factors} predates encryption as a JSONB array column (widened to TEXT in V18),
     * so a legacy value is JSON array text (e.g. {@code ["sleep","work stress"]}), not a bare
     * string - it must be parsed and re-serialized the same way
     * {@link EncryptedStringListConverter} does before encrypting, so it decrypts correctly.
     */
    private String encryptFactorsIfPlain(String rawValue, SecretKey key) {
        if (rawValue == null || encryptionService.isEncrypted(rawValue)) {
            return rawValue;
        }
        List<String> factors;
        try {
            factors = objectMapper.readValue(rawValue, STRING_LIST_TYPE);
        } catch (Exception e) {
            throw new EncryptionException("Failed to parse legacy mood_entry.factors value during backfill", e);
        }
        try {
            return encryptionService.encrypt(objectMapper.writeValueAsString(factors), key);
        } catch (Exception e) {
            throw new EncryptionException("Failed to re-serialize mood_entry.factors value during backfill", e);
        }
    }
}
