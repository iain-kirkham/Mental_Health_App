package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryCreationDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing MoodEntry entities.
 * Contains business logic for creating, retrieving, updating, and deleting mood entries.
 */
@Service
public class MoodEntryService {

    private final MoodEntryRepository moodEntryRepository;

    public MoodEntryService(MoodEntryRepository moodEntryRepository) {
        this.moodEntryRepository = moodEntryRepository;
    }

    /**
     * Converts a MoodEntryCreationDTO to a MoodEntry entity.
     * This prepares data from client input for persistence.
     *
     * @param creationRequest DTO containing the data to create a new mood entry.
     * @return A new MoodEntry entity populated with data from the DTO.
     */
    private MoodEntry toEntity(MoodEntryCreationDTO creationRequest) {
        MoodEntry entity = new MoodEntry();
        entity.setMoodScore(creationRequest.getMoodScore());
        entity.setDateTime(creationRequest.getDateTime());
        entity.setFactors(creationRequest.getFactors());
        entity.setNotes(creationRequest.getNotes());
        return entity;
    }

    /**
     * Maps a MoodEntry entity to a MoodEntryResponseDTO.
     * Converts internal entity data into a form suitable for API responses.
     *
     * @param entity The MoodEntry entity retrieved from the database.
     * @return A DTO representation of the MoodEntry for client consumption.
     */
    private MoodEntryResponseDTO toDto(MoodEntry entity) {
        MoodEntryResponseDTO moodResponse = new MoodEntryResponseDTO();
        moodResponse.setId(entity.getId());
        moodResponse.setMoodScore(entity.getMoodScore());
        moodResponse.setDateTime(entity.getDateTime());
        moodResponse.setFactors(entity.getFactors());
        moodResponse.setNotes(entity.getNotes());
        return moodResponse;
    }

    /**
     * Creates and saves a new MoodEntry.
     * Converts the incoming DTO to an entity, saves it, and returns the response DTO.
     *
     * @param creationDTO The DTO containing data for the new mood entry.
     * @return The saved mood entry as a response DTO.
     */
    public MoodEntryResponseDTO createMoodEntry(MoodEntryCreationDTO creationDTO) {
        // TODO when adding multiple users get the authenticated user ID and set it on the entity before saving
        MoodEntry moodEntryToSave = toEntity(creationDTO);
        MoodEntry savedMoodEntry = moodEntryRepository.save(moodEntryToSave);
        return toDto(savedMoodEntry);
    }

    /**
     * Retrieves all MoodEntry records from the database.
     *
     * @return A list of all mood entries converted to response DTOs.
     */
    public List<MoodEntryResponseDTO> getAllMoodEntries() {
        List<MoodEntry> moodEntities = moodEntryRepository.findAll();
        return moodEntities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves a MoodEntry by its ID.
     *
     * @param id The ID of the mood entry to retrieve.
     * @return The found mood entry converted to a response DTO.
     * @throws ResourceNotFoundException If no entry with the given ID exists.
     */
    public MoodEntryResponseDTO getMoodEntryById(Long id) {
        MoodEntry moodEntry = moodEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MoodEntry not found with ID: " + id));
        // TODO when adding multiple users add an ownership check before returning data
        return toDto(moodEntry);
    }

    /**
     * Updates an existing MoodEntry.
     *
     * @param id The ID of the mood entry to update.
     * @param updateRequest DTO containing updated mood entry data.
     * @return The updated mood entry as a response DTO.
     * @throws ResourceNotFoundException If the entry does not exist.
     */
    public MoodEntryResponseDTO updateMoodEntry(Long id, MoodEntryCreationDTO updateRequest) {
        MoodEntry existingMoodEntry = moodEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MoodEntry not found with ID: " + id));

        // TODO when adding multiple users add ownership check
        existingMoodEntry.setMoodScore(updateRequest.getMoodScore());
        existingMoodEntry.setDateTime(updateRequest.getDateTime());
        existingMoodEntry.setFactors(updateRequest.getFactors());
        existingMoodEntry.setNotes(updateRequest.getNotes());

        MoodEntry updatedMoodEntry = moodEntryRepository.save(existingMoodEntry);
        return toDto(updatedMoodEntry);
    }

    /**
     * Deletes a MoodEntry by ID.
     *
     * @param id The ID of the mood entry to delete.
     * @throws ResourceNotFoundException If no entry with the given ID exists.
     */
    public void deleteMoodEntry(Long id) {
        // TODO when adding multiple users fetch the entity first to perform ownership check
        if (!moodEntryRepository.existsById(id)) {
            throw new ResourceNotFoundException("MoodEntry not found with ID: " + id);
        }
        moodEntryRepository.deleteById(id);
    }
}
