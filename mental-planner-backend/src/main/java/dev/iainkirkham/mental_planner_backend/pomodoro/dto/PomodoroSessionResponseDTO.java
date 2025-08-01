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
     *
     */
    private Long id;
    /**
     *
     */
    private Instant startTime;
    /**
     *
     */
    private Instant endTime;
    /**
     *
     */
    private Integer duration;
    /**
     *
     */
    private Short score;
    /**
     *
     */
    private String notes;
}
