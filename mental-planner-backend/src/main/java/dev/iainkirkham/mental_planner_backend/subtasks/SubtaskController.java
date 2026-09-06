package dev.iainkirkham.mental_planner_backend.subtasks;

import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing subtasks under a parent task.
 */
@RestController
@RequestMapping("api/tasks/{taskId}/subtasks")
public class SubtaskController {

    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }

    /**
     * Creates a new subtask under a task.
     *
     * @param taskId the ID of the parent task
     * @param requestDTO the data for the new subtask
     * @return the created subtask with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<SubtaskResponseDTO> createSubtask(
            @PathVariable Long taskId, @RequestBody @Valid SubtaskRequestDTO requestDTO) {
        SubtaskResponseDTO created = subtaskService.createSubtask(taskId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates a subtask, e.g. renaming it or toggling completion.
     *
     * @param taskId the ID of the parent task
     * @param subtaskId the ID of the subtask to update
     * @param requestDTO the updated subtask data
     * @return the updated subtask with status 200 (OK)
     */
    @PutMapping("/{subtaskId}")
    public ResponseEntity<SubtaskResponseDTO> updateSubtask(
            @PathVariable Long taskId, @PathVariable Long subtaskId, @RequestBody @Valid SubtaskRequestDTO requestDTO) {
        SubtaskResponseDTO updated = subtaskService.updateSubtask(taskId, subtaskId, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a subtask.
     *
     * @param taskId the ID of the parent task
     * @param subtaskId the ID of the subtask to delete
     * @return status 204 (No Content)
     */
    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId) {
        subtaskService.deleteSubtask(taskId, subtaskId);
        return ResponseEntity.noContent().build();
    }
}
