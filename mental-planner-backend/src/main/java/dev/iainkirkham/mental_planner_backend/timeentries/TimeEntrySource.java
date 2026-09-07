package dev.iainkirkham.mental_planner_backend.timeentries;

/**
 * How a task time entry was recorded: automatically by the stopwatch, automatically by a
 * focus session, or logged by hand.
 */
public enum TimeEntrySource {
    STOPWATCH,
    MANUAL,
    FOCUS
}
