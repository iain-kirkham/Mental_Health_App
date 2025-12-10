package dev.iainkirkham.mental_planner_backend.mood.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for mood entry responses.
 * Contains all relevant data for the client, excluding sensitive server-managed fields like userId.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodEntryResponseDTO {

    private Long id;
    private Short moodScore;
    private Instant dateTime;
    private List<String> factors;
    private String notes;
}

