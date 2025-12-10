package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing mood entries.
 * Provides entry points for creation, retrieval, updating, and deletion (CRUD) of mood entry records.
 */
@RestController
@RequestMapping("api/mood")
public class MoodEntryController {

    private final MoodEntryService moodEntryService;

    public MoodEntryController(MoodEntryService moodEntryService) {
        this.moodEntryService = moodEntryService;
    }

    /**
     * Creates a new mood entry.
     *
     * @param requestDTO the data for the new entry
     * @return the created entry with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<MoodEntryResponseDTO> createMoodEntry(@RequestBody @Valid MoodEntryRequestDTO requestDTO) {
        MoodEntryResponseDTO savedEntry = moodEntryService.createMoodEntry(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
    }

    /**
     * Retrieves all mood entries, optionally filtered by date range.
     *
     * @param startDate optional start date for filtering (ISO-8601 format)
     * @param endDate optional end date for filtering (ISO-8601 format)
     * @return list of entries with status 200 (OK), or 204 (No Content) if empty
     */
    @GetMapping
    public ResponseEntity<List<MoodEntryResponseDTO>> getAllMoodEntries(
            @RequestParam(required = false) java.time.Instant startDate,
            @RequestParam(required = false) java.time.Instant endDate) {

        List<MoodEntryResponseDTO> entries;

        if (startDate != null && endDate != null) {
            entries = moodEntryService.getMoodEntriesByDateRange(startDate, endDate);
        } else {
            entries = moodEntryService.getAllMoodEntries();
        }

        if (entries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(entries);
    }

    /**
     * Retrieves a mood entry by its ID.
     *
     * @param id the ID of the entry
     * @return the entry with status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<MoodEntryResponseDTO> getMoodEntryById(@PathVariable Long id) {
        MoodEntryResponseDTO entry = moodEntryService.getMoodEntryById(id);
        return ResponseEntity.ok(entry);
    }

    /**
     * Updates an existing mood entry.
     *
     * @param id the ID of the entry to update
     * @param requestDTO the updated entry data
     * @return the updated entry with status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<MoodEntryResponseDTO> updateMoodEntry(@PathVariable Long id, @RequestBody @Valid MoodEntryRequestDTO requestDTO) {
        MoodEntryResponseDTO updated = moodEntryService.updateMoodEntry(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a mood entry by its ID.
     *
     * @param id the ID of the entry to delete
     * @return status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMoodEntry(@PathVariable Long id) {
        moodEntryService.deleteMoodEntry(id);
        return ResponseEntity.noContent().build();
    }
}
