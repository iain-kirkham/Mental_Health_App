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
    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime startTime;
    private boolean completed;
    private List<SubTaskDTO> subTasks;
}
