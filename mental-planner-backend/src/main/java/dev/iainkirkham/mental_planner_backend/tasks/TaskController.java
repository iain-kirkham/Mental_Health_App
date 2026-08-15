package dev.iainkirkham.mental_planner_backend.tasks;

import dev.iainkirkham.mental_planner_backend.tasks.dto.ActualMinutesRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.CompletionRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.SubtaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.SubtaskResponseDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskReorderItemDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskResponseDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing tasks in the daily planner.
 * Provides entry points for creation, retrieval, updating, reordering, and deletion (CRUD) of task records.
 */
@RestController
@RequestMapping("api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Creates a new task.
     *
     * @param requestDTO the data for the new task
     * @return the created task with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid TaskRequestDTO requestDTO) {
        TaskResponseDTO savedTask = taskService.createTask(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    /**
     * Retrieves tasks for a single day, or for a date range.
     *
     * @param date single day to fetch tasks for (mutually exclusive with startDate/endDate)
     * @param startDate start of a date range (inclusive)
     * @param endDate end of a date range (inclusive)
     * @return list of tasks with status 200 (OK), or 204 (No Content) if empty
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<TaskResponseDTO> tasks;

        if (startDate != null && endDate != null) {
            tasks = taskService.getTasksForDateRange(startDate, endDate);
        } else if (date != null) {
            tasks = taskService.getTasksForDate(date);
        } else {
            tasks = taskService.getTasksForDate(LocalDate.now());
        }

        if (tasks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tasks);
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param id the ID of the task
     * @return the task with status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        TaskResponseDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /**
     * Updates an existing task. Also used to reschedule a task or toggle completion.
     *
     * @param id the ID of the task to update
     * @param requestDTO the updated task data
     * @return the updated task with status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody @Valid TaskRequestDTO requestDTO) {
        TaskResponseDTO updated = taskService.updateTask(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Sets a task's completion state and cascades it to all of its subtasks, atomically.
     *
     * @param id the ID of the task to update
     * @param requestDTO the new completion state
     * @return the updated task (with cascaded subtasks) with status 200 (OK)
     */
    @PutMapping("/{id}/completion")
    public ResponseEntity<TaskResponseDTO> setCompletionCascade(
            @PathVariable Long id, @RequestBody @Valid CompletionRequestDTO requestDTO) {
        TaskResponseDTO updated = taskService.setCompletionCascade(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Updates only a task's tracked time, used by the global task stopwatch on pause/stop
     * so it never clobbers other fields that may have changed concurrently.
     *
     * @param id the ID of the task to update
     * @param requestDTO the new actualMinutes value
     * @return the updated task with status 200 (OK)
     */
    @PutMapping("/{id}/actual-minutes")
    public ResponseEntity<TaskResponseDTO> updateActualMinutes(
            @PathVariable Long id, @RequestBody @Valid ActualMinutesRequestDTO requestDTO) {
        TaskResponseDTO updated = taskService.updateActualMinutes(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Applies new sort orders to a batch of tasks, used when the backlog list is reordered.
     *
     * @param items the tasks and their new sort order
     * @return the updated tasks with status 200 (OK)
     */
    @PutMapping("/reorder")
    public ResponseEntity<List<TaskResponseDTO>> reorderTasks(@RequestBody @Valid List<TaskReorderItemDTO> items) {
        List<TaskResponseDTO> updated = taskService.reorderTasks(items);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the ID of the task to delete
     * @return status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Archives a task, removing it from planner views without deleting it.
     *
     * @param id the ID of the task to archive
     * @return the archived task with status 200 (OK)
     */
    @PutMapping("/{id}/archive")
    public ResponseEntity<TaskResponseDTO> archiveTask(@PathVariable Long id) {
        TaskResponseDTO archived = taskService.archiveTask(id);
        return ResponseEntity.ok(archived);
    }

    /**
     * Creates a new subtask under a task.
     *
     * @param taskId the ID of the parent task
     * @param requestDTO the data for the new subtask
     * @return the created subtask with status 201 (Created)
     */
    @PostMapping("/{taskId}/subtasks")
    public ResponseEntity<SubtaskResponseDTO> createSubtask(
            @PathVariable Long taskId, @RequestBody @Valid SubtaskRequestDTO requestDTO) {
        SubtaskResponseDTO created = taskService.createSubtask(taskId, requestDTO);
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
    @PutMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<SubtaskResponseDTO> updateSubtask(
            @PathVariable Long taskId, @PathVariable Long subtaskId, @RequestBody @Valid SubtaskRequestDTO requestDTO) {
        SubtaskResponseDTO updated = taskService.updateSubtask(taskId, subtaskId, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a subtask.
     *
     * @param taskId the ID of the parent task
     * @param subtaskId the ID of the subtask to delete
     * @return status 204 (No Content)
     */
    @DeleteMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId) {
        taskService.deleteSubtask(taskId, subtaskId);
        return ResponseEntity.noContent().build();
    }
}
