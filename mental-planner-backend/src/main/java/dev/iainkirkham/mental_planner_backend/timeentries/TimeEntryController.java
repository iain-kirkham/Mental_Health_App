package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TimeEntryReflectionRequestDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for logging, reflecting on, and deleting time entries for the authenticated
 * user. Top-level (not nested under a task) since a time entry's task link is optional.
 */
@RestController
@RequestMapping("api/time-entries")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    /**
     * Retrieves all time entries logged by the authenticated user within a date range, across
     * every task.
     *
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return list of time entries with status 200 (OK), or 204 (No Content) if empty
     */
    @GetMapping
    public ResponseEntity<List<TaskTimeEntryResponseDTO>> getTimeEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<TaskTimeEntryResponseDTO> entries = timeEntryService.getTimeEntries(from, to);
        if (entries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(entries);
    }

    /**
     * Logs a time entry for the authenticated user, either a completed stopwatch/Focus run or
     * a manual entry.
     *
     * @param requestDTO the time entry to log
     * @return the created time entry with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<TaskTimeEntryResponseDTO> logTimeEntry(@RequestBody @Valid TaskTimeEntryRequestDTO requestDTO) {
        TaskTimeEntryResponseDTO created = timeEntryService.logTimeEntry(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Attaches a Focus session's post-run reflection to an already-logged time entry.
     *
     * @param entryId the ID of the time entry
     * @param requestDTO the reflection fields to set
     * @return the updated time entry with status 200 (OK)
     */
    @PatchMapping("/{entryId}")
    public ResponseEntity<TaskTimeEntryResponseDTO> updateTimeEntryReflection(
            @PathVariable Long entryId, @RequestBody @Valid TimeEntryReflectionRequestDTO requestDTO) {
        TaskTimeEntryResponseDTO updated = timeEntryService.updateTimeEntryReflection(entryId, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a time entry.
     *
     * @param entryId the ID of the time entry to delete
     * @return status 204 (No Content)
     */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable Long entryId) {
        timeEntryService.deleteTimeEntry(entryId);
        return ResponseEntity.noContent().build();
    }
}
