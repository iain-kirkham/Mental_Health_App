package dev.iainkirkham.mental_planner_backend.timeentries.dto;

import dev.iainkirkham.mental_planner_backend.timeentries.EnergyRating;
import dev.iainkirkham.mental_planner_backend.timeentries.TimeEntrySource;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for logging a task time entry: a completed stopwatch/Focus run, or a manual entry.
 * Excludes id, which is derived from the path and managed by the server. taskId is optional -
 * a Focus session can be run without linking it to a task - except for a MANUAL entry, which
 * has no task to apply its minutes to without one.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskTimeEntryRequestDTO {

    private Long taskId;

    private Instant startedAt;

    private Instant endedAt;

    @NotNull(message = "minutes is required")
    @Min(value = 1, message = "minutes must be at least 1")
    private Integer minutes;

    @NotNull(message = "entryDate is required")
    private LocalDate entryDate;

    @NotNull(message = "source is required")
    private TimeEntrySource source;

    private String notes;

    @Min(value = 1, message = "score must be at least 1")
    @Max(value = 5, message = "score must be at most 5")
    private Short score;

    private EnergyRating energyRating;

    @AssertTrue(message = "taskId is required for a manual entry")
    public boolean isTaskIdPresentWhenManual() {
        return source != TimeEntrySource.MANUAL || taskId != null;
    }
}
