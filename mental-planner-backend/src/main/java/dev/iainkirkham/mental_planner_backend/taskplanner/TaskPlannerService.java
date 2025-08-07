package dev.iainkirkham.mental_planner_backend.taskplanner;

import dev.iainkirkham.mental_planner_backend.taskplanner.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing Task and SubTask entities.
 * Handles business logic for creating, retrieving, updating, and deleting tasks and subtasks.
 */
@Service
public class TaskPlannerService {

    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;

    public TaskPlannerService(TaskRepository taskRepository, SubTaskRepository subTaskRepository) {
        this.taskRepository = taskRepository;
        this.subTaskRepository = subTaskRepository;
    }

    /**
     * Converts a Task entity to a TaskDTO.
     */
    private TaskDTO convertToTaskDTO(Task task) {
        if (task == null) {
            return null;
        }
        List<SubTaskDTO> subTaskDTOs = task.getSubTasks().stream()
                .map(this::convertToSubTaskDTO)
                .collect(Collectors.toList());

        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDate(),
                task.getStartTime(),
                task.isCompleted(),
                subTaskDTOs
        );
    }

    /**
     * Converts a SubTask entity to a SubTaskDTO.
     */
    private SubTaskDTO convertToSubTaskDTO(SubTask subTask) {
        if (subTask == null) {
            return null;
        }
        return new SubTaskDTO(
                subTask.getId(),
                subTask.getTitle(),
                subTask.isCompleted()
        );
    }

    /**
     * Converts a TaskDTO to a Task entity.
     * This prepares the DTO data for persistence.
     *
     * @param taskDTO The DTO with input data.
     * @return A new Task entity.
     */
    private Task convertToTaskEntity(TaskDTO taskDTO) {
        if (taskDTO == null) {
            return null;
        }
        Task task = new Task();
        task.setId(taskDTO.getId()); // Will be null for creation
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setDate(taskDTO.getDate());
        task.setStartTime(taskDTO.getStartTime());
        task.setCompleted(taskDTO.isCompleted());

        if (taskDTO.getSubTasks() != null) {
            taskDTO.getSubTasks().forEach(subTaskDto -> {
                SubTask subTask = convertToSubTaskEntity(subTaskDto);
                task.addSubTask(subTask);
            });
        }
        return task;
    }

    /**
     * Updates an existing Task entity from a TaskDTO.
     * This shapes internal data for safe exposure to the client.
     *
     * @param existingTask The task entity from the database.
     * @param taskDTO The DTO containing updated task data.
     */
    private void updateTaskEntityFromDTO(Task existingTask, TaskDTO taskDTO) {
        if (existingTask == null || taskDTO == null) {
            return;
        }
        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setDate(taskDTO.getDate());
        existingTask.setStartTime(taskDTO.getStartTime());
        existingTask.setCompleted(taskDTO.isCompleted());

        existingTask.getSubTasks().clear();
        if (taskDTO.getSubTasks() != null) {
            taskDTO.getSubTasks().forEach(subTaskDto -> {
                SubTask subTask = convertToSubTaskEntity(subTaskDto);
                existingTask.addSubTask(subTask);
            });
        }
    }

    /**
     * Converts a SubTaskDTO to a SubTask entity.
     */
    private SubTask convertToSubTaskEntity(SubTaskDTO subTaskDto) {
        if (subTaskDto == null) {
            return null;
        }
        SubTask subTask = new SubTask();
        subTask.setId(subTaskDto.getId());
        subTask.setTitle(subTaskDto.getTitle());
        subTask.setCompleted(subTaskDto.isCompleted());
        return subTask;
    }

    /**
     * Retrieves all tasks from the database.
     *
     * @return A list of TaskDTO objects representing all tasks.
     */
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single task by its ID.
     *
     * @param id The ID of the task.
     * @return An Optional containing the TaskDTO if found.
     */
    public Optional<TaskDTO> getTaskById(Long id) {
        return taskRepository.findById(id).map(this::convertToTaskDTO);
    }

    /**
     * Retrieves all tasks for a specific date.
     *
     * @param date The date to search for tasks.
     * @return A list of TaskDTO objects for the specified date.
     */
    public List<TaskDTO> getTasksByDate(LocalDate date) {
        return taskRepository.findByDate(date).stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all tasks within a date range.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of TaskDTO objects within the date range.
     */
    public List<TaskDTO> getTasksForWeek(LocalDate startDate, LocalDate endDate) {
        return taskRepository.findByDateBetween(startDate, endDate).stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new task.
     * Converts the creation request to an entity, saves it, then returns a response DTO.
     *
     * @param taskCreateRequest The request containing data to create a new task.
     * @return The created task as a TaskDTO.
     */
    @Transactional
    public TaskDTO createTask(TaskCreateRequest taskCreateRequest) {
        Task task = convertToTaskEntity(taskCreateRequest);
        Task savedTask = taskRepository.save(task);
        return convertToTaskDTO(savedTask);
    }

    /**
     * Updates an existing task.
     *
     * @param id The ID of the task to update.
     * @param taskDTO The DTO containing updated task data.
     * @return An Optional containing the updated TaskDTO if found.
     */
    @Transactional
    public Optional<TaskDTO> updateTask(Long id, TaskDTO taskDTO) {
        return taskRepository.findById(id).map(existingTask -> {
            updateTaskEntityFromDTO(existingTask, taskDTO);
            Task savedTask = taskRepository.save(existingTask);
            return convertToTaskDTO(savedTask);
        });
    }

    /**
     * Deletes a task by ID.
     *
     * @param id The ID of the task to delete.
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * Updates the completion status of a task.
     *
     * @param id The ID of the task to update.
     * @param completed The new completion status.
     * @return An Optional containing the updated TaskDTO if found.
     */
    @Transactional
    public Optional<TaskDTO> updateTaskCompletion(Long id, boolean completed) {
        return taskRepository.findById(id).map(task -> {
            task.setCompleted(completed);
            return convertToTaskDTO(taskRepository.save(task));
        });
    }

    /**
     * Retrieves all subtasks for a specific task.
     *
     * @param taskId The ID of the parent task.
     * @return A list of SubTaskDTO objects belonging to the task.
     */
    public List<SubTaskDTO> getSubTasksByTaskId(Long taskId) {
        return subTaskRepository.findByTaskId(taskId).stream()
                .map(this::convertToSubTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates the completion status of a subtask.
     *
     * @param subTaskId The ID of the subtask to update.
     * @param completed The new completion status.
     * @return An Optional containing the updated SubTaskDTO if found.
     */
    @Transactional
    public Optional<SubTaskDTO> updateSubTaskCompletion(Long subTaskId, boolean completed) {
        return subTaskRepository.findById(subTaskId).map(subTask -> {
            subTask.setCompleted(completed);
            return convertToSubTaskDTO(subTaskRepository.save(subTask));
        });
    }

    /**
     * Adds a new subtask to an existing task.
     *
     * @param taskId The ID of the parent task.
     * @param subTaskDTO The DTO containing subtask data to create.
     * @return An Optional containing the created SubTaskDTO if parent task found.
     */
    @Transactional
    public Optional<SubTaskDTO> addSubTaskToTask(Long taskId, SubTaskDTO subTaskDTO) {
        return taskRepository.findById(taskId).map(task -> {
            SubTask subTask = convertToSubTaskEntity(subTaskDTO);
            task.addSubTask(subTask);
            SubTask savedSubTask = subTaskRepository.save(subTask);
            return convertToSubTaskDTO(savedSubTask);
        });
    }

    /**
     * Deletes a subtask by ID.
     *
     * @param subTaskId The ID of the subtask to delete.
     */
    @Transactional
    public void deleteSubTask(Long subTaskId) {
        subTaskRepository.findById(subTaskId).ifPresent(subTask -> {
            Task parentTask = subTask.getTask();
            if (parentTask != null) {
                parentTask.removeSubTask(subTask);
            }
            subTaskRepository.delete(subTask);
        });
    }
}