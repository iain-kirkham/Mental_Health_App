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
    private Long id;
    private String title;
    private boolean completed;
}