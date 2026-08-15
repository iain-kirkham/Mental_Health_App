package dev.iainkirkham.mental_planner_backend.tasks.dto;

import dev.iainkirkham.mental_planner_backend.tasks.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for incoming task requests (create/update).
 * Excludes id and userId which are managed by the server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Scheduled date is required")
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
}
