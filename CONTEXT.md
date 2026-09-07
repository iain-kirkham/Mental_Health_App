# Mental Planner

A task planner and mental-health tracking app: users schedule Tasks onto a daily timeline, track time against them, log moods, and run focused work sessions.

## Language

**Task**:
A user-scheduled unit of work with a title, a scheduled date, and optional planned/actual time. Owns Subtasks and TaskTimeEntries.

**TaskTimeEntry**:
A single logged record of time spent on a Task — one Stopwatch run, one Focus session, or one hand-entered amount. Recorded as history; whether it also changes the Task's Actual minutes depends on its source.

**Focus session**:
A fixed-length timed run tracked against a Task, optionally rated afterward with a quality score (1–5) and an energy rating (energized/drained), plus free-text notes. Merges what used to be two separate things — a plain countdown timer and a standalone Pomodoro session — into one TaskTimeEntry source; see [ADR 0002](docs/adr/0002-unify-pomodoro-into-time-entries.md).
_Avoid_: Pomodoro session, countdown session

**Timer session** (planned — not yet implemented, see [ADR 0002](docs/adr/0002-unify-pomodoro-into-time-entries.md)):
The server-tracked record of an in-progress Stopwatch or Focus run. Intended to replace the current client-computed Actual minutes total so a run's persist can be reconciled against a concurrent manual entry instead of overwriting it; see [ADR 0001](docs/adr/0001-actual-minutes-checkpoint-stays-client-trusted.md).

**Actual minutes**:
A Task's running total of tracked time. Changed only through three operations: applying a manual entry (adds), unwinding a manual entry (subtracts, floored at 0, used when that entry is deleted), and recording a timer checkpoint (sets the total directly, from a Stopwatch or Focus run's elapsed time). A Stopwatch/Focus TaskTimeEntry is history only — it never itself changes Actual minutes, since a checkpoint already did.
_Avoid_: tracked time, actualMinutes (field name, not the concept)
