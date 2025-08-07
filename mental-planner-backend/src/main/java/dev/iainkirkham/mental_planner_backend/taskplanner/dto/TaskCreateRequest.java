package dev.iainkirkham.mental_planner_backend.taskplanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO for task creation with validation constraints.
 */
public class TaskCreateRequest extends TaskDTO {
    @NotBlank(message = "Title is required")
    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @NotNull(message = "Date is required")
    @Override
    public LocalDate getDate() {
        return super.getDate();
    }
}
