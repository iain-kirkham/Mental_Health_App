package dev.iainkirkham.mental_planner_backend.pomodoro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for Pomodoro session responses.
 * Contains all relevant data for the client, excluding sensitive server-managed fields like userId.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PomodoroSessionResponseDTO {

    private Long id;
    private Instant startTime;
    private Instant endTime;
    private int duration;
    private Short score;
    private String notes;
}

