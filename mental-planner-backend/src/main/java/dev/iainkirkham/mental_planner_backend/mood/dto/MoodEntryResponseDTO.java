package dev.iainkirkham.mental_planner_backend.mood.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

/**
 * DTO representing a user's mood entry.
 * Used for returning structured mood data in API responses.
 */
@Getter @Setter @NoArgsConstructor @ToString
public class MoodEntryResponseDTO {

    /**
     * The unique identifier for the mood entry
     */
    private Long id;

    /**
     *  The recorded Mood score from 1 to 5
     */
    private Short moodScore;

    /**
     * Date and time the mood entry was recorded in UTC.
     */
    private Instant dateTime;

    /**
     *  Factors associated with the user's e.g ("tired", "work)
     */
    private List<String> factors;

    /**
     * Any additional notes for this mood entry.
     */
    private String notes;
}
