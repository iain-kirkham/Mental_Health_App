package dev.iainkirkham.mental_planner_backend.timeentries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskTimeEntryRepository extends JpaRepository<TaskTimeEntry, Long> {

    /**
     * Find a specific time entry by ID, scoped to a task and user.
     * @param id the time entry ID
     * @param taskId the parent task's ID
     * @param userId the Clerk user ID
     * @return Optional containing the entry if found and belongs to the user's task
     */
    Optional<TaskTimeEntry> findByIdAndTaskIdAndUserId(Long id, Long taskId, String userId);

    /**
     * Find all time entries for a task, scoped to a user, most recent day first.
     * @param taskId the parent task's ID
     * @param userId the Clerk user ID
     * @return list of entries ordered by entry date then creation time, descending
     */
    List<TaskTimeEntry> findByTaskIdAndUserIdOrderByEntryDateDescCreatedAtDesc(Long taskId, String userId);
}
