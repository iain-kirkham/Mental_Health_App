package dev.iainkirkham.mental_planner_backend.taskplanner.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for subtask operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubTaskDTO {
    /**
     * The unique identifier for the subtask.
     */
    private Long id;
    /**
     * The title or name of the subtask.
     */
    private String title;
    /**
     * Indicates whether the user has completed this subtask.
     */
    private boolean completed;
}