package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import dev.iainkirkham.mental_planner_backend.config.OwnedEntityLookup;
import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.tasks.Task;
import dev.iainkirkham.mental_planner_backend.tasks.TaskRepository;
import dev.iainkirkham.mental_planner_backend.tasks.TaskService;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing TaskTimeEntry records, scoped to a parent Task owned by the
 * authenticated user.
 */
@Service
@Transactional(readOnly = true)
public class TimeEntryService {

    private final TaskTimeEntryRepository taskTimeEntryRepository;
    private final TaskTimeEntryMapper taskTimeEntryMapper;
    private final TaskRepository taskRepository;
    private final OwnedEntityLookup ownedEntityLookup;
    private final AuthenticationContext authenticationContext;
    private final TaskService taskService;

    public TimeEntryService(TaskTimeEntryRepository taskTimeEntryRepository,
                             TaskTimeEntryMapper taskTimeEntryMapper,
                             TaskRepository taskRepository,
                             OwnedEntityLookup ownedEntityLookup,
                             AuthenticationContext authenticationContext,
                             TaskService taskService) {
        this.taskTimeEntryRepository = taskTimeEntryRepository;
        this.taskTimeEntryMapper = taskTimeEntryMapper;
        this.taskRepository = taskRepository;
        this.ownedEntityLookup = ownedEntityLookup;
        this.authenticationContext = authenticationContext;
        this.taskService = taskService;
    }

    /**
     * Looks up a task by ID, verifying it belongs to the authenticated user. Used (rather than
     * a separate {@link TaskService#assertOwnedByCurrentUser} check) by the methods below that
     * also need to mutate the Task itself, so ownership is verified and the entity fetched in
     * one query instead of two.
     */
    private Task findOwnedTask(Long taskId) {
        return ownedEntityLookup.findOwnedOrThrow(taskRepository::findByIdAndUserId, taskId, "Task");
    }

    /**
     * Retrieves all time entries logged against a task owned by the authenticated user,
     * most recent day first.
     *
     * @param taskId The parent task's ID.
     * @return The task's time entries as response DTOs.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    public List<TaskTimeEntryResponseDTO> getTimeEntries(Long taskId) {
        taskService.assertOwnedByCurrentUser(taskId);
        String userId = authenticationContext.getCurrentUserId();
        return taskTimeEntryMapper.toResponseDTOList(
                taskTimeEntryRepository.findByTaskIdAndUserIdOrderByEntryDateDescCreatedAtDesc(taskId, userId));
    }

    /**
     * Logs a time entry against a task owned by the authenticated user. A stopwatch/countdown
     * entry is recorded as history only, since its run is already reflected in the tracked
     * total via the separate {@link Task#recordTimerCheckpoint} persist. A manual entry has no
     * other path to update the total, so it's applied via {@link Task#applyManualEntry}.
     *
     * @param taskId The parent task's ID.
     * @param requestDTO The time entry to log.
     * @return The saved entry as a response DTO.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public TaskTimeEntryResponseDTO logTimeEntry(Long taskId, TaskTimeEntryRequestDTO requestDTO) {
        Task task = findOwnedTask(taskId);
        TaskTimeEntry entry = taskTimeEntryMapper.toEntity(requestDTO, taskId);
        entry.setId(null);
        entry.setUserId(authenticationContext.getCurrentUserId());
        TaskTimeEntry saved = taskTimeEntryRepository.save(entry);

        if (requestDTO.getSource() == TimeEntrySource.MANUAL) {
            task.applyManualEntry(requestDTO.getMinutes());
            taskRepository.save(task);
        }

        return taskTimeEntryMapper.toResponseDTO(saved);
    }

    /**
     * Deletes a time entry belonging to a task owned by the authenticated user. Deleting a
     * manual entry unwinds its minutes from the tracked total via {@link Task#unwindManualEntry}
     * (clamped at 0); deleting a stopwatch/countdown entry only removes the history row, since
     * the total was already kept correct by that run's own {@link Task#recordTimerCheckpoint}
     * persist.
     *
     * @param taskId The parent task's ID.
     * @param entryId The time entry's ID.
     * @throws ResourceNotFoundException if the parent task or entry doesn't exist / isn't owned by the user.
     */
    @Transactional
    public void deleteTimeEntry(Long taskId, Long entryId) {
        Task task = findOwnedTask(taskId);
        String userId = authenticationContext.getCurrentUserId();
        TaskTimeEntry entry = taskTimeEntryRepository.findByIdAndTaskIdAndUserId(entryId, taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Time entry not found with ID: " + entryId));

        if (entry.getSource() == TimeEntrySource.MANUAL) {
            task.unwindManualEntry(entry.getMinutes());
            taskRepository.save(task);
        }

        taskTimeEntryRepository.delete(entry);
    }
}
