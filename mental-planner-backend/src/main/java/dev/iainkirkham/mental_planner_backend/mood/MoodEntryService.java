package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing MoodEntry entities.
 * Contains business logic for creating, retrieving, updating, and deleting mood entries.
 * All operations are filtered by the authenticated user to ensure data isolation.
 */
@Service
public class MoodEntryService {

    private final MoodEntryRepository moodEntryRepository;
    private final AuthenticationContext authenticationContext;
    private final MoodEntryMapper moodEntryMapper;

    public MoodEntryService(MoodEntryRepository moodEntryRepository,
                           AuthenticationContext authenticationContext,
                           MoodEntryMapper moodEntryMapper) {
        this.moodEntryRepository = moodEntryRepository;
        this.authenticationContext = authenticationContext;
        this.moodEntryMapper = moodEntryMapper;
    }

    /**
     * Creates and saves a new MoodEntry for the authenticated user.
     *
     * @param requestDTO The mood entry DTO to create.
     * @return The saved mood entry as a response DTO.
     */
    public MoodEntryResponseDTO createMoodEntry(MoodEntryRequestDTO requestDTO) {
        MoodEntry moodEntry = moodEntryMapper.toEntity(requestDTO);
        moodEntry.setId(null); // Ensure ID is null for new entries
        // Automatically set userId from authenticated user
        moodEntry.setUserId(authenticationContext.getCurrentUserId());
        MoodEntry savedEntry = moodEntryRepository.save(moodEntry);
        return moodEntryMapper.toResponseDTO(savedEntry);
    }

    /**
     * Retrieves all MoodEntry records for the authenticated user, ordered by date time descending.
     *
     * @return A list of all mood entries as response DTOs belonging to the current user.
     */
    public List<MoodEntryResponseDTO> getAllMoodEntries() {
        String userId = authenticationContext.getCurrentUserId();
        List<MoodEntry> entries = moodEntryRepository.findByUserIdOrderByDateTimeDesc(userId);
        return moodEntryMapper.toResponseDTOList(entries);
    }

    /**
     * Retrieves mood entries for the authenticated user within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return A list of mood entries as response DTOs within the date range, ordered by date time descending.
     */
    public List<MoodEntryResponseDTO> getMoodEntriesByDateRange(java.time.Instant startDate, java.time.Instant endDate) {
        String userId = authenticationContext.getCurrentUserId();
        List<MoodEntry> entries = moodEntryRepository.findByUserIdAndDateTimeBetweenOrderByDateTimeDesc(
            userId, startDate, endDate
        );
        return moodEntryMapper.toResponseDTOList(entries);
    }

    /**
     * Retrieves a MoodEntry by its ID if it belongs to the authenticated user.
     *
     * @param id The ID of the mood entry to retrieve.
     * @return The found mood entry as a response DTO.
     * @throws ResourceNotFoundException If no entry with the given ID exists or doesn't belong to the user.
     */
    public MoodEntryResponseDTO getMoodEntryById(Long id) {
        String userId = authenticationContext.getCurrentUserId();
        MoodEntry entry = moodEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("MoodEntry not found with ID: " + id));
        return moodEntryMapper.toResponseDTO(entry);
    }

    /**
     * Updates an existing MoodEntry if it belongs to the authenticated user.
     *
     * @param id The ID of the mood entry to update.
     * @param requestDTO The DTO with updated data.
     * @return The updated mood entry as a response DTO.
     * @throws ResourceNotFoundException If the entry does not exist or doesn't belong to the user.
     */
    public MoodEntryResponseDTO updateMoodEntry(Long id, MoodEntryRequestDTO requestDTO) {
        String userId = authenticationContext.getCurrentUserId();
        MoodEntry existingMoodEntry = moodEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("MoodEntry not found with ID: " + id));

        moodEntryMapper.updateEntityFromDTO(existingMoodEntry, requestDTO);

        MoodEntry updatedEntry = moodEntryRepository.save(existingMoodEntry);
        return moodEntryMapper.toResponseDTO(updatedEntry);
    }

    /**
     * Deletes a MoodEntry by ID if it belongs to the authenticated user.
     *
     * @param id The ID of the mood entry to delete.
     * @throws ResourceNotFoundException If no entry with the given ID exists or doesn't belong to the user.
     */
    public void deleteMoodEntry(Long id) {
        String userId = authenticationContext.getCurrentUserId();
        MoodEntry entry = moodEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("MoodEntry not found with ID: " + id));
        moodEntryRepository.delete(entry);
    }
}
