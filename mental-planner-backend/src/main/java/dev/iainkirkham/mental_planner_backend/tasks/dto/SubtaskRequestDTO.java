package dev.iainkirkham.mental_planner_backend.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming subtask requests (create/update). Excludes id and taskId,
 * which are derived from the path and managed by the server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private boolean completed;

    private int sortOrder;

    private Integer plannedMinutes;
}
