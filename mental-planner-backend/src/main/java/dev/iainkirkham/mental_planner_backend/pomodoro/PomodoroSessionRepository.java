package dev.iainkirkham.mental_planner_backend.pomodoro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {

    /**
     * Find all pomodoro sessions for a specific user.
     * @param userId the Clerk user ID
     * @return list of pomodoro sessions belonging to the user
     */
    List<PomodoroSession> findByUserId(String userId);

    /**
     * Find a specific pomodoro session by ID that belongs to a specific user.
     * @param id the pomodoro session ID
     * @param userId the Clerk user ID
     * @return Optional containing the session if found and belongs to the user
     */
    Optional<PomodoroSession> findByIdAndUserId(Long id, String userId);

    /**
     * Delete a pomodoro session by ID that belongs to a specific user.
     * @param id the pomodoro session ID
     * @param userId the Clerk user ID
     */
    void deleteByIdAndUserId(Long id, String userId);

    /**
     * Find pomodoro sessions for a user within a date range.
     * @param userId the Clerk user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of sessions within the date range, ordered by start time descending
     */
    List<PomodoroSession> findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(
        String userId, 
        java.time.Instant startDate, 
        java.time.Instant endDate
    );

    /**
     * Find all pomodoro sessions for a user ordered by start time.
     * @param userId the Clerk user ID
     * @return list of all sessions ordered by start time descending
     */
    List<PomodoroSession> findByUserIdOrderByStartTimeDesc(String userId);
}
