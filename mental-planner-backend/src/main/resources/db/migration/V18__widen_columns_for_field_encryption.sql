-- Widen columns that will hold encrypted values (base64(IV||ciphertext||tag) is larger
-- than the plaintext, and no longer bounded by a meaningful business length).
-- Does not re-encrypt or otherwise touch any existing data in these columns - only newly
-- written values are encrypted from this point on. See docs/security/data-at-rest-encryption-plan.md.

ALTER TABLE task ALTER COLUMN title TYPE TEXT;
ALTER TABLE task ALTER COLUMN description TYPE TEXT;
ALTER TABLE task ALTER COLUMN category TYPE TEXT;

ALTER TABLE subtask ALTER COLUMN title TYPE TEXT;

ALTER TABLE pomodoro_session ALTER COLUMN notes TYPE TEXT;

ALTER TABLE mood_entry ALTER COLUMN factors TYPE TEXT USING factors::text;
