# Mental Planner

A task planner and mental-health tracking app: users schedule Tasks onto a daily timeline, track time against them, log moods, and run focused work sessions.

## Language

**Task**:
A user-scheduled unit of work with a title, a scheduled date, and optional planned/actual time. Owns Subtasks and TaskTimeEntries.

**TaskTimeEntry**:
A single logged record of time spent on a Task — one stopwatch run, one countdown/focus session, or one hand-entered amount. Recorded as history; whether it also changes the Task's Actual minutes depends on its source.

**Actual minutes**:
A Task's running total of tracked time. Changed only through three operations: applying a manual entry (adds), unwinding a manual entry (subtracts, floored at 0, used when that entry is deleted), and recording a timer checkpoint (sets the total directly, from a stopwatch or countdown run's elapsed time). A stopwatch/countdown TaskTimeEntry is history only — it never itself changes Actual minutes, since a checkpoint already did.
_Avoid_: tracked time, actualMinutes (field name, not the concept)
