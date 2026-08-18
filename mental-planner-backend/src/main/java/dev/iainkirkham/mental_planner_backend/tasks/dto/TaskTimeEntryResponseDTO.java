package dev.iainkirkham.mental_planner_backend.tasks.dto;

import dev.iainkirkham.mental_planner_backend.tasks.TimeEntrySource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskTimeEntryResponseDTO {

    private Long id;
    private Long taskId;
    private Instant startedAt;
    private Instant endedAt;
    private int minutes;
    private LocalDate entryDate;
    private TimeEntrySource source;
    private String note;
}
