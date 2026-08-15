package dev.iainkirkham.mental_planner_backend.tasks;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    Optional<Subtask> findByIdAndTaskId(Long id, Long taskId);

    List<Subtask> findByTaskIdOrderBySortOrderAsc(Long taskId);

    /**
     * Batch-fetch subtasks for a set of tasks (used when listing tasks for a day/week),
     * ordered so callers can group by taskId while preserving sort order within each group.
     */
    List<Subtask> findByTaskIdInOrderByTaskIdAscSortOrderAsc(List<Long> taskIds);
}
