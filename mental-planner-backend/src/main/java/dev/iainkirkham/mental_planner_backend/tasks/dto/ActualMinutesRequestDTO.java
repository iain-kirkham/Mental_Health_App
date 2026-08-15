package dev.iainkirkham.mental_planner_backend.tasks.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating only a task's tracked time, used by the global task stopwatch so a
 * pause/stop persist can never clobber other fields (e.g. completed) that changed
 * concurrently elsewhere.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualMinutesRequestDTO {

    @NotNull(message = "actualMinutes is required")
    @Min(value = 0, message = "actualMinutes must not be negative")
    private Integer actualMinutes;
}
