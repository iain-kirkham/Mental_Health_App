package dev.iainkirkham.mental_planner_backend.mood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object for creating a new mood entry
 */
@Getter @Setter @NoArgsConstructor @ToString
public class MoodEntryCreationDTO {

    /**
     * Mood score represents the user's mood at the time of entry.
     * Must be a valid integer between 1 (very bad) and 5 (very good).
     */
    @Min(1)
    @Max(5)
    @NotNull
    private Short moodScore;

    /**
     * The date and time of when the mood entry was recorded.
     * The time is provided in UTC.
     */
    @NotNull
    private Instant dateTime;

    /**
     * List of factors contributing to the user's mood.
     * Each string in the list corresponds to a specific factor such as sleep or work.
     */
    private List<String> factors;

    /**
     * String containing any notes on the mood entry (optional).
     */
    private String notes;
}
