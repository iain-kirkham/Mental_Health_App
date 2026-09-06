package dev.iainkirkham.mental_planner_backend.tasks.dto;

import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskResponseDTO;
import dev.iainkirkham.mental_planner_backend.tasks.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for task responses.
 * Contains all relevant data for the client, excluding sensitive server-managed fields like userId.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDate scheduledDate;
    private Instant startTime;
    private Instant endTime;
    private boolean completed;
    private int sortOrder;
    private Integer plannedMinutes;
    private int actualMinutes;
    private String category;
    private boolean archived;
    private TaskPriority priority;
    private List<SubtaskResponseDTO> subtasks;
}
