package dev.iainkirkham.mental_planner_backend.tasks;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A single logged interval of time tracked against a task - either one continuous
 * stopwatch run (start to stop, spanning any internal pauses) or a manually-logged entry.
 */
@Entity
@Table(name = "task_time_entry")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TaskTimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "user_id")
    private String userId;

    /**
     * When the stopwatch run started. Null for manual entries.
     */
    @Column(name = "started_at")
    private Instant startedAt;

    /**
     * When the stopwatch run ended. Null for manual entries.
     */
    @Column(name = "ended_at")
    private Instant endedAt;

    @NotNull
    @Column(nullable = false)
    private int minutes;

    /**
     * The day this entry counts toward. A separate column (rather than derived from
     * startedAt) since manual entries may have no start/end timestamps at all.
     */
    @NotNull
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TimeEntrySource source;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
