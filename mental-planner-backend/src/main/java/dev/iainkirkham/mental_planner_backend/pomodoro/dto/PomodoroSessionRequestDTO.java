package dev.iainkirkham.mental_planner_backend.pomodoro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for incoming Pomodoro session requests (create/update).
 * Excludes id and userId which are managed by the server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PomodoroSessionRequestDTO {

    @NotNull(message = "Start time is required")
    private Instant startTime;

    private Instant endTime;

    @Min(value = 0, message = "Duration must be at least 0")
    private int duration;

    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    private Short score;

    private String notes;
}

