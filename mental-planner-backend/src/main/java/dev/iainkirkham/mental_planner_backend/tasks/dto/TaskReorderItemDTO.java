package dev.iainkirkham.mental_planner_backend.tasks.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single task's new sort order, used for batch reordering of the backlog list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskReorderItemDTO {

    @NotNull(message = "Task id is required")
    private Long id;

    private int sortOrder;
}
