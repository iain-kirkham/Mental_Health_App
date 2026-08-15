package dev.iainkirkham.mental_planner_backend.tasks.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for toggling a task's completion state along with a cascade to all of its
 * subtasks, applied atomically in a single transaction to avoid the race condition
 * of firing the parent update and each subtask update as separate concurrent requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletionRequestDTO {

    @NotNull(message = "completed is required")
    private Boolean completed;
}
