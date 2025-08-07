package dev.iainkirkham.mental_planner_backend.taskplanner.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Base DTO for task operations containing common fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    /**
     * The unique identifier for the task.
     */
    private Long id;
    /**
     * The title or name of the task.
     */
    private String title;
    /**
     * Optional detailed description of the task.
     */
    private String description;
    /**
     * The date when the task is scheduled to be completed.
     */
    private LocalDate date;
    /**
     * Optional start time for the task.
     */
    private LocalTime startTime;
    /**
     * Indicates whether the user has completed this task.
     */
    private boolean completed;
    /**
     * List of subtasks associated with this task.
     */
    private List<SubTaskDTO> subTasks;
}
