-- Folds the pomodoro_session table into task_time_entry: COUNTDOWN and PomodoroSession both
-- represented the same real-world action (a fixed-length focus timer) as two disconnected
-- records. See docs/adr/0002-unify-pomodoro-into-time-entries.md.

ALTER TABLE task_time_entry ADD COLUMN score SMALLINT;
ALTER TABLE task_time_entry ADD COLUMN energy_rating VARCHAR(20);

-- note (VARCHAR(500), plain) becomes notes (TEXT, encrypted via EncryptedStringConverter) -
-- widened to match pomodoro_session.notes, since base64(IV||ciphertext||tag) exceeds 500 chars.
ALTER TABLE task_time_entry RENAME COLUMN note TO notes;
ALTER TABLE task_time_entry ALTER COLUMN notes TYPE TEXT;

-- A Focus session can now be run without a linked task (pomodoro_session already allowed this).
ALTER TABLE task_time_entry ALTER COLUMN task_id DROP NOT NULL;

-- task_time_entry's FK was ON DELETE CASCADE; pomodoro_session's was ON DELETE SET NULL. Now
-- that task_id is nullable, prefer SET NULL so deleting a task doesn't retroactively erase
-- logged time - re-add the FK with the new delete rule.
ALTER TABLE task_time_entry DROP CONSTRAINT task_time_entry_task_id_fkey;
ALTER TABLE task_time_entry ADD CONSTRAINT task_time_entry_task_id_fkey
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE SET NULL;

UPDATE task_time_entry SET source = 'FOCUS' WHERE source = 'COUNTDOWN';

-- entry_date has no equivalent on pomodoro_session; derived from start_time in UTC, which can
-- misfile a late-evening session by a day for users behind UTC - accepted for this historical
-- backfill, since no user timezone is stored anywhere in the schema.
-- legacy_unknown rows (V5's pre-user_id sentinel) are skipped: every read is user_id-scoped, so
-- they'd be permanently invisible, and their notes can't be decrypted under any real user's DEK.
INSERT INTO task_time_entry
    (task_id, user_id, started_at, ended_at, minutes, entry_date,
     source, notes, score, energy_rating, created_at)
SELECT p.task_id, p.user_id, p.start_time, p.end_time,
       GREATEST(COALESCE(p.duration, 0), 0),
       (p.start_time AT TIME ZONE 'UTC')::date,
       'FOCUS', p.notes, p.score, p.energy_rating,
       COALESCE(p.end_time, p.start_time)
FROM pomodoro_session p
WHERE p.user_id <> 'legacy_unknown';

DROP TABLE pomodoro_session;
