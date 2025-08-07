package dev.iainkirkham.mental_planner_backend.pomodoro.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class PomodoroSessionResponseDTO {
    /**
     * The unique identifier for the session.
     */
    private Long id;
    /**
     * The time the session started.
     */
    private Instant startTime;
    /**
     * The time the session ended.
     */
    private Instant endTime;
    /**
     * The duration of the session in minutes.
     */
    private Integer duration;
    /**
     * The score for the session from 1 (very bad) to 5 (very good).
     */
    private Short score;
    /**
     * Optional notes about the session.
     */
    private String notes;
}
