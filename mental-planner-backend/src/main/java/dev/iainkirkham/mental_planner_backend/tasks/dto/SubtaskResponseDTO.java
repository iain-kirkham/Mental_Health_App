package dev.iainkirkham.mental_planner_backend.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskResponseDTO {

    private Long id;
    private Long taskId;
    private String title;
    private boolean completed;
    private int sortOrder;
    private Integer plannedMinutes;
}
