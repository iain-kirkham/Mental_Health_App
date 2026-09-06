package dev.iainkirkham.mental_planner_backend.timeentries.dto;

import dev.iainkirkham.mental_planner_backend.timeentries.TimeEntrySource;
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
