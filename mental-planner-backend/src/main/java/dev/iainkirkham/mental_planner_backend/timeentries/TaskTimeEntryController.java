package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for a single task's time-entry history. Creating, updating, and deleting
 * entries happens through {@link TimeEntryController} instead, since a time entry's task link
 * is optional.
 */
@RestController
@RequestMapping("api/tasks/{taskId}/time-entries")
public class TaskTimeEntryController {

    private final TimeEntryService timeEntryService;

    public TaskTimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    /**
     * Retrieves all time entries logged against a task.
     *
     * @param taskId the ID of the parent task
     * @return list of time entries with status 200 (OK), or 204 (No Content) if empty
     */
    @GetMapping
    public ResponseEntity<List<TaskTimeEntryResponseDTO>> getTimeEntriesForTask(@PathVariable Long taskId) {
        List<TaskTimeEntryResponseDTO> entries = timeEntryService.getTimeEntriesForTask(taskId);
        if (entries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(entries);
    }
}
