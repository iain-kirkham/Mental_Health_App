package dev.iainkirkham.mental_planner_backend.tasks;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import dev.iainkirkham.mental_planner_backend.config.OwnedEntityLookup;
import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.subtasks.SubtaskService;
import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskResponseDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.ActualMinutesRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.CompletionRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskReorderItemDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing Task entities.
 * Handles business logic for creating, retrieving, updating, and deleting tasks.
 * All operations are filtered by the authenticated user to ensure data isolation.
 */
@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final AuthenticationContext authenticationContext;
    private final OwnedEntityLookup ownedEntityLookup;
    private final TaskMapper taskMapper;
    private final SubtaskService subtaskService;

    public TaskService(TaskRepository taskRepository,
                        AuthenticationContext authenticationContext,
                        OwnedEntityLookup ownedEntityLookup,
                        TaskMapper taskMapper,
                        SubtaskService subtaskService) {
        this.taskRepository = taskRepository;
        this.authenticationContext = authenticationContext;
        this.ownedEntityLookup = ownedEntityLookup;
        this.taskMapper = taskMapper;
        this.subtaskService = subtaskService;
    }

    /**
     * Attaches each task's subtasks (batch-fetched) onto its response DTO.
     */
    private List<TaskResponseDTO> withSubtasks(List<Task> tasks) {
        List<TaskResponseDTO> dtos = taskMapper.toResponseDTOList(tasks);
        if (dtos.isEmpty()) {
            return dtos;
        }

        List<Long> taskIds = tasks.stream().map(Task::getId).toList();
        Map<Long, List<SubtaskResponseDTO>> subtasksByTaskId = subtaskService.findByTaskIds(taskIds);

        dtos.forEach(dto -> dto.setSubtasks(subtasksByTaskId.getOrDefault(dto.getId(), List.of())));
        return dtos;
    }

    /**
     * Attaches a single task's subtasks (fresh-fetched) onto its response DTO.
     */
    private TaskResponseDTO withSubtasks(Task task) {
        return withSubtasks(List.of(task)).get(0);
    }

    /**
     * Creates a new task for the authenticated user.
     *
     * @param requestDTO The task DTO to create.
     * @return The saved task as a response DTO.
     */
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO requestDTO) {
        Task task = taskMapper.toEntity(requestDTO);
        task.setId(null); // Ensure ID is null for new entries
        // Automatically set userId from authenticated user
        task.setUserId(authenticationContext.getCurrentUserId());
        Task savedTask = taskRepository.save(task);
        TaskResponseDTO dto = taskMapper.toResponseDTO(savedTask);
        dto.setSubtasks(List.of());
        return dto;
    }

    /**
     * Retrieves all of the authenticated user's non-archived tasks scheduled for a given day.
     *
     * @param date the day to fetch tasks for
     * @return A list of tasks as response DTOs belonging to the current user.
     */
    public List<TaskResponseDTO> getTasksForDate(LocalDate date) {
        String userId = authenticationContext.getCurrentUserId();
        List<Task> tasks = taskRepository.findByUserIdAndScheduledDateAndArchivedFalseOrderBySortOrderAsc(userId, date);
        return withSubtasks(tasks);
    }

    /**
     * Retrieves the authenticated user's non-archived tasks within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return A list of tasks as response DTOs within the date range.
     */
    public List<TaskResponseDTO> getTasksForDateRange(LocalDate startDate, LocalDate endDate) {
        String userId = authenticationContext.getCurrentUserId();
        List<Task> tasks = taskRepository.findByUserIdAndScheduledDateBetweenAndArchivedFalseOrderByScheduledDateAscSortOrderAsc(
            userId, startDate, endDate
        );
        return withSubtasks(tasks);
    }

    /**
     * Retrieves a single task by its ID if it belongs to the authenticated user.
     *
     * @param id The ID of the task.
     * @return The found task as a response DTO.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    public TaskResponseDTO getTaskById(Long id) {
        Task task = findOwnedTask(id);
        return withSubtasks(task);
    }

    /**
     * Looks up a task by ID, verifying it belongs to the authenticated user.
     */
    private Task findOwnedTask(Long id) {
        return ownedEntityLookup.findOwnedOrThrow(taskRepository::findByIdAndUserId, id, "Task");
    }

    /**
     * Verifies that a task exists and belongs to the authenticated user.
     * Used by other feature packages (e.g. pomodoro, time entries) that link records to a
     * task without needing access to the task itself.
     *
     * @param id The ID of the task.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    public void assertOwnedByCurrentUser(Long id) {
        findOwnedTask(id);
    }

    /**
     * Updates an existing task if it belongs to the authenticated user.
     * Also used to reschedule a task (change start/end time) or toggle completion.
     *
     * @param id The ID of the task to update.
     * @param requestDTO The DTO with updated data.
     * @return The updated task as a response DTO.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO requestDTO) {
        Task existingTask = findOwnedTask(id);

        taskMapper.updateEntityFromDTO(existingTask, requestDTO);

        Task updatedTask = taskRepository.save(existingTask);
        return withSubtasks(updatedTask);
    }

    /**
     * Archives a task, removing it from day/week planner views without deleting it.
     *
     * @param id The ID of the task to archive.
     * @return The archived task as a response DTO.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public TaskResponseDTO archiveTask(Long id) {
        Task task = findOwnedTask(id);
        task.setArchived(true);
        Task saved = taskRepository.save(task);
        return withSubtasks(saved);
    }

    /**
     * Updates only a task's tracked time. Used by the global task stopwatch so a
     * pause/stop persist can never clobber other fields that changed concurrently
     * elsewhere (e.g. completion state), unlike a full {@link #updateTask} replace.
     *
     * @param id The ID of the task to update.
     * @param requestDTO The new actualMinutes value.
     * @return The updated task as a response DTO.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public TaskResponseDTO updateActualMinutes(Long id, ActualMinutesRequestDTO requestDTO) {
        Task task = findOwnedTask(id);
        task.recordTimerCheckpoint(requestDTO.getActualMinutes());
        Task saved = taskRepository.save(task);
        return withSubtasks(saved);
    }

    /**
     * Sets a task's completion state and cascades the same state to all of its subtasks,
     * atomically in one transaction. Used instead of a separate PUT per subtask (which
     * raced against the parent's own update and could leave a random subset of subtasks
     * out of sync with what the client had just set optimistically).
     *
     * @param id The ID of the task to update.
     * @param requestDTO The new completion state.
     * @return The updated task, with all subtasks reflecting the same completion state.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public TaskResponseDTO setCompletionCascade(Long id, CompletionRequestDTO requestDTO) {
        Task task = findOwnedTask(id);
        task.setCompleted(requestDTO.getCompleted());
        Task savedTask = taskRepository.save(task);

        List<SubtaskResponseDTO> updatedSubtasks = subtaskService.setCompletionForTask(id, requestDTO.getCompleted());

        TaskResponseDTO dto = taskMapper.toResponseDTO(savedTask);
        dto.setSubtasks(updatedSubtasks);
        return dto;
    }

    /**
     * Applies a batch of new sort orders to the authenticated user's tasks, used when the
     * backlog list is reordered by drag-and-drop.
     *
     * @param items the tasks and their new sort order
     * @return the updated tasks as response DTOs
     * @throws ResourceNotFoundException if any task doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public List<TaskResponseDTO> reorderTasks(List<TaskReorderItemDTO> items) {
        List<Long> requestedIds = items.stream().map(TaskReorderItemDTO::getId).distinct().toList();
        String userId = authenticationContext.getCurrentUserId();
        Map<Long, Task> ownedTasksById = taskRepository.findByIdInAndUserId(requestedIds, userId).stream()
                .collect(Collectors.toMap(Task::getId, task -> task));

        if (ownedTasksById.size() < requestedIds.size()) {
            List<Long> missingIds = requestedIds.stream().filter(id -> !ownedTasksById.containsKey(id)).toList();
            throw new ResourceNotFoundException("Task(s) not found with ID(s): " + missingIds);
        }

        List<Task> updatedTasks = items.stream()
                .map(item -> {
                    Task task = ownedTasksById.get(item.getId());
                    task.setSortOrder(item.getSortOrder());
                    return task;
                })
                .toList();

        List<Task> savedTasks = taskRepository.saveAll(updatedTasks);
        return withSubtasks(savedTasks);
    }

    /**
     * Deletes a task by ID if it belongs to the authenticated user.
     *
     * @param id The ID of the task to delete.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.delete(findOwnedTask(id));
    }
}
