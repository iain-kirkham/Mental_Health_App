package dev.iainkirkham.mental_planner_backend.timeentries.dto;

import dev.iainkirkham.mental_planner_backend.timeentries.EnergyRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for attaching a Focus session's post-run reflection to an already-logged time entry.
 * Deliberately narrow - only score/energyRating/notes are settable this way, so a run's
 * minutes/task/source can never drift from what was recorded at completion time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryReflectionRequestDTO {

    @Min(value = 1, message = "score must be at least 1")
    @Max(value = 5, message = "score must be at most 5")
    private Short score;

    private String notes;

    private EnergyRating energyRating;
}
