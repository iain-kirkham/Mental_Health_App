package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryCreationDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing mood entries.
 * Provides entry points for creation, retrieval, updating, and deletion (CRUD) of mood entry srecords.
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
     * @param creationDTO the data for the new entry
     * @return the created entry with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<MoodEntryResponseDTO> createMoodEntry(@RequestBody @Valid MoodEntryCreationDTO creationDTO) {
        MoodEntryResponseDTO responseDTO = moodEntryService.createMoodEntry(creationDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /**
     * Retrieves all mood entries.
     *
     * @return list of entries with status 200 (OK), or 204 (No Content) if empty
     */
    @GetMapping
    public ResponseEntity<List<MoodEntryResponseDTO>> getAllMoodEntries() {
        List<MoodEntryResponseDTO> dtos = moodEntryService.getAllMoodEntries();
        if (dtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    /**
     * Retrieves a mood entry by its ID.
     *
     * @param id the ID of the entry
     * @return the entry with status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<MoodEntryResponseDTO> getMoodEntryById(@PathVariable Long id) {
        MoodEntryResponseDTO dto = moodEntryService.getMoodEntryById(id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * Updates an existing mood entry.
     *
     * @param id the ID of the entry to update
     * @param updateDTO the updated entry data
     * @return the updated entry with status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<MoodEntryResponseDTO> updateMoodEntry(@PathVariable Long id, @RequestBody @Valid MoodEntryCreationDTO updateDTO) {
        MoodEntryResponseDTO responseDTO = moodEntryService.updateMoodEntry(id, updateDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
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
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
