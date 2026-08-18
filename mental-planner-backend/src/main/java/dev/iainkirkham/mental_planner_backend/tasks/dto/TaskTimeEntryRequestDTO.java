package dev.iainkirkham.mental_planner_backend.tasks.dto;

import dev.iainkirkham.mental_planner_backend.tasks.TimeEntrySource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for logging a task time entry, either a completed stopwatch run or a manual entry.
 * Excludes id and taskId, which are derived from the path and managed by the server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskTimeEntryRequestDTO {

    private Instant startedAt;

    private Instant endedAt;

    @NotNull(message = "minutes is required")
    @Min(value = 1, message = "minutes must be at least 1")
    private Integer minutes;

    @NotNull(message = "entryDate is required")
    private LocalDate entryDate;

    @NotNull(message = "source is required")
    private TimeEntrySource source;

    private String note;
}
