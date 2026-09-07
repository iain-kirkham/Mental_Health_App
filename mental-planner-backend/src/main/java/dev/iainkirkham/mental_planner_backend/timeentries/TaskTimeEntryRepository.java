package dev.iainkirkham.mental_planner_backend.timeentries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskTimeEntryRepository extends JpaRepository<TaskTimeEntry, Long> {

    /**
     * Find a specific time entry by ID, scoped to a user - taskId-agnostic, since a Focus
     * session's entry may not have a task at all.
     * @param id the time entry ID
     * @param userId the Clerk user ID
     * @return Optional containing the entry if found and belongs to the user
     */
    Optional<TaskTimeEntry> findByIdAndUserId(Long id, String userId);

    /**
     * Find all time entries for a task, scoped to a user, most recent day first.
     * @param taskId the parent task's ID
     * @param userId the Clerk user ID
     * @return list of entries ordered by entry date then creation time, descending
     */
    List<TaskTimeEntry> findByTaskIdAndUserIdOrderByEntryDateDescCreatedAtDesc(Long taskId, String userId);

    /**
     * Find all time entries for a user within a date range, across every task (and task-less
     * entries), most recent day first.
     * @param userId the Clerk user ID
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return list of entries ordered by entry date then creation time, descending
     */
    List<TaskTimeEntry> findByUserIdAndEntryDateBetweenOrderByEntryDateDescCreatedAtDesc(
            String userId, LocalDate from, LocalDate to);
}
