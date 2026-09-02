package dev.iainkirkham.mental_planner_backend.tasks;

/**
 * How a task time entry was recorded: automatically by the stopwatch, automatically by a
 * countdown/focus session, or logged by hand.
 */
public enum TimeEntrySource {
    STOPWATCH,
    MANUAL,
    COUNTDOWN
}
