package dev.iainkirkham.mental_planner_backend.tasks;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "subtask")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @NotNull
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    /**
     * Optional estimated duration in minutes for this subtask.
     */
    @Column(name = "planned_minutes")
    private Integer plannedMinutes;
}
