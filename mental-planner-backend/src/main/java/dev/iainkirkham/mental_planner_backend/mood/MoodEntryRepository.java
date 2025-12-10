package dev.iainkirkham.mental_planner_backend.mood;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {

    /**
     * Find all mood entries for a specific user.
     * @param userId the Clerk user ID
     * @return list of mood entries belonging to the user
     */
    List<MoodEntry> findByUserId(String userId);

    /**
     * Find a specific mood entry by ID that belongs to a specific user.
     * @param id the mood entry ID
     * @param userId the Clerk user ID
     * @return Optional containing the mood entry if found and belongs to the user
     */
    Optional<MoodEntry> findByIdAndUserId(Long id, String userId);

    /**
     * Delete a mood entry by ID that belongs to a specific user.
     * @param id the mood entry ID
     * @param userId the Clerk user ID
     */
    void deleteByIdAndUserId(Long id, String userId);

    /**
     * Find mood entries for a user within a date range.
     * @param userId the Clerk user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of entries within the date range, ordered by date time descending
     */
    List<MoodEntry> findByUserIdAndDateTimeBetweenOrderByDateTimeDesc(
        String userId,
        java.time.Instant startDate,
        java.time.Instant endDate
    );

    /**
     * Find all mood entries for a user ordered by date time.
     * @param userId the Clerk user ID
     * @return list of all entries ordered by date time descending
     */
    List<MoodEntry> findByUserIdOrderByDateTimeDesc(String userId);
}
