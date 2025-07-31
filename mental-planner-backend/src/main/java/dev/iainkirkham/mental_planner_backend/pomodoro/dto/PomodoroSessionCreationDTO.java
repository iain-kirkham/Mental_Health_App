package dev.iainkirkham.mental_planner_backend.pomodoro.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PomodoroSessionCreationDTO {
    /**
     * The date and time of when the pomodoro session was started.
     * The time is provided in UTC.
     */
    @NotNull
    private Instant startTime;

    /**
     * The date and time of when the pomodoro session ended.
     * The time is provided in UTC.
     */
    private Instant endTime;

    /**
     * The duration of the pomodoro session in minutes
     */
    @NotNull
    @PositiveOrZero
    private Integer duration;

    /**
     * The score for the session from 1 (very bad) to 5 (very good)
     */
    @Min(1)
    @Max(5)
    private Short score;

    /**
     * String containing any notes on the pomodoro session (optional).
     */
    private String notes;
}
