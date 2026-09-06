package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing time entries logged against a task.
 */
@RestController
@RequestMapping("api/tasks/{taskId}/time-entries")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    /**
     * Retrieves all time entries logged against a task.
     *
     * @param taskId the ID of the parent task
     * @return list of time entries with status 200 (OK), or 204 (No Content) if empty
     */
    @GetMapping
    public ResponseEntity<List<TaskTimeEntryResponseDTO>> getTimeEntries(@PathVariable Long taskId) {
        List<TaskTimeEntryResponseDTO> entries = timeEntryService.getTimeEntries(taskId);
        if (entries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(entries);
    }

    /**
     * Logs a time entry against a task, either a completed stopwatch/countdown run or a manual entry.
     *
     * @param taskId the ID of the parent task
     * @param requestDTO the time entry to log
     * @return the created time entry with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<TaskTimeEntryResponseDTO> logTimeEntry(
            @PathVariable Long taskId, @RequestBody @Valid TaskTimeEntryRequestDTO requestDTO) {
        TaskTimeEntryResponseDTO created = timeEntryService.logTimeEntry(taskId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Deletes a time entry.
     *
     * @param taskId the ID of the parent task
     * @param entryId the ID of the time entry to delete
     * @return status 204 (No Content)
     */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable Long taskId, @PathVariable Long entryId) {
        timeEntryService.deleteTimeEntry(taskId, entryId);
        return ResponseEntity.noContent().build();
    }
}
