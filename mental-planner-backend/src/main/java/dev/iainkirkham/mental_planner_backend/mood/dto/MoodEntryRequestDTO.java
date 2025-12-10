package dev.iainkirkham.mental_planner_backend.mood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for incoming mood entry requests (create/update).
 * Excludes id and userId which are managed by the server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodEntryRequestDTO {

    @NotNull(message = "Mood score is required")
    @Min(value = 1, message = "Mood score must be at least 1")
    @Max(value = 5, message = "Mood score must be at most 5")
    private Short moodScore;

    @NotNull(message = "Date and time is required")
    private Instant dateTime;

    private List<String> factors;

    private String notes;
}

