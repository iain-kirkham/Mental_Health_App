package dev.iainkirkham.mental_planner_backend.taskplanner;

import dev.iainkirkham.mental_planner_backend.taskplanner.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskPlannerController {

    private final TaskPlannerService taskPlannerService;

    @Autowired
    public TaskPlannerController(TaskPlannerService taskPlannerService) {
        this.taskPlannerService = taskPlannerService;
    }

    /**
     * Retrieves all tasks.
     *
     * @return 200 (OK) with list of TaskDTO
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskPlannerService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param id the ID of the task
     * @return 200 (OK) with TaskDTO if found, otherwise 404 (Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return taskPlannerService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves tasks for a specific date.
     *
     * @param date the date (ISO yyyy-MM-dd)
     * @return 200 (OK) with list of TaskDTO
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<TaskDTO>> getTasksByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TaskDTO> tasks = taskPlannerService.getTasksByDate(date);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Retrieves tasks within an inclusive date range.
     *
     * @param startDate range start (ISO yyyy-MM-dd)
     * @param endDate   range end (ISO yyyy-MM-dd)
     * @return 200 (OK) with list of TaskDTO
     */
    @GetMapping("/week")
    public ResponseEntity<List<TaskDTO>> getTasksForWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TaskDTO> tasks = taskPlannerService.getTasksForWeek(startDate, endDate);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Creates a new task.
     *
     * @param taskCreateRequest the data for the new task
     * @return 201 (Created) with created TaskDTO
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskCreateRequest taskCreateRequest) {
        TaskDTO createdTask = taskPlannerService.createTask(taskCreateRequest);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * Updates an existing task.
     *
     * @param id      the ID of the task to update
     * @param taskDTO the updated task data
     * @return 200 (OK) with updated TaskDTO if found, otherwise 404 (Not Found)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id,
                                              @RequestBody TaskDTO taskDTO) {
        return taskPlannerService.updateTask(id, taskDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a task by ID.
     *
     * @param id the ID of the task to delete
     * @return 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskPlannerService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates only the completion status of a task.
     *
     * @param id        the ID of the task
     * @param completed new completion status
     * @return 200 (OK) with updated TaskDTO if found, otherwise 404 (Not Found)
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskDTO> updateTaskCompletion(@PathVariable Long id,
                                                        @RequestParam boolean completed) {
        return taskPlannerService.updateTaskCompletion(id, completed)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all subtasks for a specific task.
     *
     * @param taskId the ID of the parent task
     * @return 200 (OK) with list of SubTaskDTO
     */
    @GetMapping("/{taskId}/subtasks")
    public ResponseEntity<List<SubTaskDTO>> getSubTasksByTaskId(@PathVariable Long taskId) {
        List<SubTaskDTO> subTasks = taskPlannerService.getSubTasksByTaskId(taskId);
        return ResponseEntity.ok(subTasks);
    }

    /**
     * Adds a subtask to a task.
     *
     * @param taskId     the ID of the parent task
     * @param subTaskDTO the subtask data
     * @return 201 (Created) with created SubTaskDTO if parent found, otherwise 404 (Not Found)
     */
    @PostMapping("/{taskId}/subtasks")
    public ResponseEntity<SubTaskDTO> addSubTaskToTask(@PathVariable Long taskId,
                                                       @RequestBody SubTaskDTO subTaskDTO) {
        return taskPlannerService.addSubTaskToTask(taskId, subTaskDTO)
                .map(s -> new ResponseEntity<>(s, HttpStatus.CREATED))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates only the completion status of a subtask.
     *
     * @param subTaskId the ID of the subtask
     * @param completed new completion status
     * @return 200 (OK) with updated SubTaskDTO if found, otherwise 404 (Not Found)
     */
    @PatchMapping("/subtasks/{subTaskId}/complete")
    public ResponseEntity<SubTaskDTO> updateSubTaskCompletion(@PathVariable Long subTaskId,
                                                              @RequestParam boolean completed) {
        return taskPlannerService.updateSubTaskCompletion(subTaskId, completed)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a subtask by ID.
     *
     * @param subTaskId the ID of the subtask to delete
     * @return 204 (No Content)
     */
    @DeleteMapping("/subtasks/{subTaskId}")
    public ResponseEntity<Void> deleteSubTask(@PathVariable Long subTaskId) {
        taskPlannerService.deleteSubTask(subTaskId);
        return ResponseEntity.noContent().build();
    }
}
