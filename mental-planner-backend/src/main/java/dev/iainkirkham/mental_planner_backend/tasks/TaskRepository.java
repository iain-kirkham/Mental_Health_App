package dev.iainkirkham.mental_planner_backend.tasks;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find a specific task by ID that belongs to a specific user.
     * @param id the task ID
     * @param userId the Clerk user ID
     * @return Optional containing the task if found and belongs to the user
     */
    Optional<Task> findByIdAndUserId(Long id, String userId);

    /**
     * Batch-fetch tasks by ID, scoped to a specific user, used to validate an entire
     * reorder batch in one query instead of one lookup per item.
     * @param ids the task IDs
     * @param userId the Clerk user ID
     * @return the subset of matching tasks that belong to the user
     */
    List<Task> findByIdInAndUserId(List<Long> ids, String userId);

    /**
     * Find all of a user's non-archived tasks scheduled for a given day, by sort order.
     * @param userId the Clerk user ID
     * @param scheduledDate the day to fetch tasks for
     * @return list of tasks ordered by sort order
     */
    List<Task> findByUserIdAndScheduledDateAndArchivedFalseOrderBySortOrderAsc(String userId, LocalDate scheduledDate);

    /**
     * Find all of a user's non-archived tasks scheduled within a date range.
     * @param userId the Clerk user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of tasks ordered by date then sort order
     */
    List<Task> findByUserIdAndScheduledDateBetweenAndArchivedFalseOrderByScheduledDateAscSortOrderAsc(
        String userId,
        LocalDate startDate,
        LocalDate endDate
    );
}
