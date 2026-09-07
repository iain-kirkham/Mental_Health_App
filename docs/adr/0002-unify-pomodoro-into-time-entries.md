# Pomodoro sessions fold into TaskTimeEntry as a Focus session

Status: proposed

`PomodoroSession` and `TimeEntrySource.COUNTDOWN` had drifted into two separately-built representations of the same real-world action — a fixed-length focus timer — with `PomodoroSession` alone carrying session-quality feedback (score, energy rating, notes) and never touching `Task.actualMinutes` or the `TaskTimeEntry` history at all. We're merging them: `COUNTDOWN` and `PomodoroSession` become one `TimeEntrySource.FOCUS`, and every `TaskTimeEntry` gains optional `score`/`energyRating`/`notes` fields (populated for `FOCUS`, unused for `STOPWATCH`/`MANUAL`). This gives every focus run session-quality feedback and a place in the unified per-task/per-day history, instead of the feedback living in a table nothing else reads from.

This is the first piece of the planned Sunsama-style time-tracking rework (per-day Actual-time history across all tasks, not just per-task). The other two pieces, tracked here rather than as separate ADRs since they follow from the same decision:

- **Server-tracked timer sessions**, closing the gap left by [ADR 0001](./0001-actual-minutes-checkpoint-stays-client-trusted.md): the client currently computes and sends an absolute `actualMinutes` total for stopwatch/focus runs, which can race a concurrent manual entry. Once `PomodoroSession`'s standalone start/end record is gone, a session's start needs a new home — an explicit server-side timer session (start/pause/resume/stop) is that home, and lets the server reconcile against manual entries at persist time instead of trusting a client-computed total.
- **A per-day, cross-task Actual-time view** — `TimeEntryService.getTimeEntries` is scoped to a single task today; the day view needs a new cross-task, date-ranged query so all `TaskTimeEntry` rows (stopwatch, focus, manual) for a given day show up in one place, matching the Sunsama-style history the user wants.

## Considered options

- **Keep `PomodoroSession` separate, add a shared read-model for the day view.** Rejected: perpetuates two vocabularies for the same action, and the day view would need to union two schemas indefinitely instead of querying one.

## Consequences

- Existing `PomodoroSession` rows need a backfill migration into `TaskTimeEntry` (source `FOCUS`) before the table is dropped.
- `COUNTDOWN` as a `TimeEntrySource` value goes away; anything reading it (frontend timer store, history UI) needs updating to `FOCUS`.
- The frontend Pomodoro history page and the per-task time-entry history page converge into one view once the day view ships — until then they can coexist reading from the same underlying `FOCUS`/`STOPWATCH`/`MANUAL` entries.
- The server-tracked timer session is a prerequisite for safely reconciling `FOCUS`/`STOPWATCH` persists against manual entries; don't ship the `FOCUS` merge's persist path without it if the race described in ADR 0001 needs to be closed at the same time (it can also ship after, as a follow-up, since the race already exists today for `COUNTDOWN`/`STOPWATCH`).
