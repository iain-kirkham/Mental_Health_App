package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import dev.iainkirkham.mental_planner_backend.config.OwnedEntityLookup;
import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.tasks.Task;
import dev.iainkirkham.mental_planner_backend.tasks.TaskRepository;
import dev.iainkirkham.mental_planner_backend.tasks.TaskService;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TimeEntryReflectionRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for managing TaskTimeEntry records, scoped to the authenticated user - and
 * optionally to a parent Task the user owns, since a Focus session's entry may have no task
 * link at all.
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
     * Looks up a time entry by ID, verifying it belongs to the authenticated user - taskId-
     * agnostic, since a Focus session's entry may not have a task at all.
     */
    private TaskTimeEntry findOwnedEntry(Long entryId) {
        String userId = authenticationContext.getCurrentUserId();
        return taskTimeEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Time entry not found with ID: " + entryId));
    }

    /**
     * Retrieves all time entries logged against a task owned by the authenticated user,
     * most recent day first.
     *
     * @param taskId The parent task's ID.
     * @return The task's time entries as response DTOs.
     * @throws ResourceNotFoundException if the task doesn't exist or doesn't belong to the user.
     */
    public List<TaskTimeEntryResponseDTO> getTimeEntriesForTask(Long taskId) {
        taskService.assertOwnedByCurrentUser(taskId);
        String userId = authenticationContext.getCurrentUserId();
        return taskTimeEntryMapper.toResponseDTOList(
                taskTimeEntryRepository.findByTaskIdAndUserIdOrderByEntryDateDescCreatedAtDesc(taskId, userId));
    }

    /**
     * Retrieves all time entries logged by the authenticated user within a date range, across
     * every task (and task-less entries), most recent day first.
     *
     * @param from The start date (inclusive).
     * @param to The end date (inclusive).
     * @return The matching time entries as response DTOs.
     */
    public List<TaskTimeEntryResponseDTO> getTimeEntries(LocalDate from, LocalDate to) {
        String userId = authenticationContext.getCurrentUserId();
        return taskTimeEntryMapper.toResponseDTOList(
                taskTimeEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDescCreatedAtDesc(
                        userId, from, to));
    }

    /**
     * Logs a time entry for the authenticated user, either a completed stopwatch/Focus run or
     * a manual entry. A stopwatch/Focus entry is recorded as history only, since its run is
     * already reflected in the tracked total via the separate {@link Task#recordTimerCheckpoint}
     * persist. A manual entry has no other path to update the total, so it's applied via
     * {@link Task#applyManualEntry} - {@link TaskTimeEntryRequestDTO}'s own validation rejects a
     * manual entry with no taskId before this is ever reached.
     *
     * @param requestDTO The time entry to log.
     * @return The saved entry as a response DTO.
     * @throws ResourceNotFoundException if taskId is set but doesn't belong to the user.
     */
    @Transactional
    public TaskTimeEntryResponseDTO logTimeEntry(TaskTimeEntryRequestDTO requestDTO) {
        Task task = requestDTO.getTaskId() != null ? findOwnedTask(requestDTO.getTaskId()) : null;

        TaskTimeEntry entry = taskTimeEntryMapper.toEntity(requestDTO);
        entry.setId(null);
        entry.setUserId(authenticationContext.getCurrentUserId());
        TaskTimeEntry saved = taskTimeEntryRepository.save(entry);

        if (requestDTO.getSource() == TimeEntrySource.MANUAL && task != null) {
            task.applyManualEntry(requestDTO.getMinutes());
            taskRepository.save(task);
        }

        return taskTimeEntryMapper.toResponseDTO(saved);
    }

    /**
     * Attaches a Focus session's post-run reflection (score/energyRating/notes) to an
     * already-logged time entry. Deliberately narrow: minutes/task/source can never be changed
     * this way.
     *
     * @param entryId The time entry's ID.
     * @param requestDTO The reflection fields to set.
     * @return The updated entry as a response DTO.
     * @throws ResourceNotFoundException if the entry doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public TaskTimeEntryResponseDTO updateTimeEntryReflection(Long entryId, TimeEntryReflectionRequestDTO requestDTO) {
        TaskTimeEntry entry = findOwnedEntry(entryId);
        entry.setScore(requestDTO.getScore());
        entry.setEnergyRating(requestDTO.getEnergyRating());
        entry.setNotes(requestDTO.getNotes());
        TaskTimeEntry updated = taskTimeEntryRepository.save(entry);
        return taskTimeEntryMapper.toResponseDTO(updated);
    }

    /**
     * Deletes a time entry belonging to the authenticated user. Deleting a manual entry unwinds
     * its minutes from its task's tracked total via {@link Task#unwindManualEntry} (clamped at
     * 0); deleting a stopwatch/Focus entry only removes the history row, since the total was
     * already kept correct by that run's own {@link Task#recordTimerCheckpoint} persist.
     *
     * @param entryId The time entry's ID.
     * @throws ResourceNotFoundException if the entry doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public void deleteTimeEntry(Long entryId) {
        TaskTimeEntry entry = findOwnedEntry(entryId);

        if (entry.getSource() == TimeEntrySource.MANUAL && entry.getTaskId() != null) {
            Task task = findOwnedTask(entry.getTaskId());
            task.unwindManualEntry(entry.getMinutes());
            taskRepository.save(task);
        }

        taskTimeEntryRepository.delete(entry);
    }
}
