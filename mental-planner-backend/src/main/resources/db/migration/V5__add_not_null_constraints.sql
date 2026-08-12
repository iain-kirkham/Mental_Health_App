-- Add NOT NULL constraints to user_id columns now that authentication is enforced.
-- First backfill any existing NULL values (from before auth was added).
UPDATE mood_entry SET user_id = 'legacy_unknown' WHERE user_id IS NULL;
UPDATE pomodoro_session SET user_id = 'legacy_unknown' WHERE user_id IS NULL;

-- Now apply NOT NULL constraints
ALTER TABLE mood_entry ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE pomodoro_session ALTER COLUMN user_id SET NOT NULL;

-- Align pomodoro_session.start_time with entity @NotNull constraint
UPDATE pomodoro_session SET start_time = end_time WHERE start_time IS NULL;
ALTER TABLE pomodoro_session ALTER COLUMN start_time SET NOT NULL;

-- Align pomodoro_session.duration with entity @Min(0) constraint
ALTER TABLE pomodoro_session ALTER COLUMN duration SET NOT NULL;

