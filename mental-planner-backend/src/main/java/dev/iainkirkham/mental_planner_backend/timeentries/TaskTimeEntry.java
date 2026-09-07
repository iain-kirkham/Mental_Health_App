package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.security.EncryptedStringConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A single logged interval of time tracked against a task - a continuous stopwatch run
 * (start to stop, spanning any internal pauses), a fixed-length Focus session (optionally
 * rated afterward with a score/energy rating/notes), or a manually-logged entry. The task
 * link is optional: a Focus session can be run without linking it to any task.
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

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "user_id")
    private String userId;

    /**
     * When the stopwatch/focus run started. Null for manual entries.
     */
    @Column(name = "started_at")
    private Instant startedAt;

    /**
     * When the stopwatch/focus run ended. Null for manual entries.
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

    /**
     * Free-text notes about this entry - the manual-entry note, or a Focus session's
     * post-run reflection. Optional for every source.
     */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    @ToString.Exclude
    private String notes;

    /**
     * A Focus session's quality score, 1 (very bad) to 5 (very good). Null for other sources,
     * and for a Focus session until its reflection is submitted.
     */
    @Min(1)
    @Max(5)
    @Column(name = "score")
    private Short score;

    /**
     * A Focus session's optional lightweight rating of whether it left the user feeling
     * energized or drained. Null for other sources, and for a Focus session until its
     * reflection is submitted.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "energy_rating", length = 20)
    private EnergyRating energyRating;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
