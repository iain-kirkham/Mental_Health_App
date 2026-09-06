package dev.iainkirkham.mental_planner_backend.subtasks;

import dev.iainkirkham.mental_planner_backend.config.OwnedEntityLookup;
import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskResponseDTO;
import dev.iainkirkham.mental_planner_backend.tasks.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing Subtask entities, scoped to a parent Task owned by the
 * authenticated user. Verifies task ownership itself (rather than depending on TaskService)
 * since TaskService depends on this class for read assembly - a two-way dependency would be
 * a circular bean reference.
 */
@Service
@Transactional(readOnly = true)
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final SubtaskMapper subtaskMapper;
    private final TaskRepository taskRepository;
    private final OwnedEntityLookup ownedEntityLookup;

    public SubtaskService(SubtaskRepository subtaskRepository,
                           SubtaskMapper subtaskMapper,
                           TaskRepository taskRepository,
                           OwnedEntityLookup ownedEntityLookup) {
        this.subtaskRepository = subtaskRepository;
        this.subtaskMapper = subtaskMapper;
        this.taskRepository = taskRepository;
        this.ownedEntityLookup = ownedEntityLookup;
    }

    private void assertTaskOwnedByCurrentUser(Long taskId) {
        ownedEntityLookup.findOwnedOrThrow(taskRepository::findByIdAndUserId, taskId, "Task");
    }

    /**
     * Batch-fetches subtasks for a set of tasks, grouped by task ID. Used by TaskService to
     * attach subtasks onto task response DTOs; callers are expected to have already verified
     * ownership of each task.
     *
     * @param taskIds the parent tasks' IDs
     * @return subtasks grouped by task ID, each list ordered by sort order
     */
    public Map<Long, List<SubtaskResponseDTO>> findByTaskIds(List<Long> taskIds) {
        return subtaskRepository.findByTaskIdInOrderByTaskIdAscSortOrderAsc(taskIds)
                .stream()
                .map(subtaskMapper::toResponseDTO)
                .collect(Collectors.groupingBy(SubtaskResponseDTO::getTaskId));
    }

    /**
     * Adds a new subtask to a task owned by the authenticated user.
     *
     * @param taskId The parent task's ID.
     * @param requestDTO The subtask data to create.
     * @return The created subtask as a response DTO.
     * @throws ResourceNotFoundException if the parent task doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public SubtaskResponseDTO createSubtask(Long taskId, SubtaskRequestDTO requestDTO) {
        assertTaskOwnedByCurrentUser(taskId);
        Subtask subtask = subtaskMapper.toEntity(requestDTO, taskId);
        subtask.setId(null);
        Subtask saved = subtaskRepository.save(subtask);
        return subtaskMapper.toResponseDTO(saved);
    }

    /**
     * Updates a subtask belonging to a task owned by the authenticated user.
     *
     * @param taskId The parent task's ID.
     * @param subtaskId The subtask's ID.
     * @param requestDTO The updated subtask data.
     * @return The updated subtask as a response DTO.
     * @throws ResourceNotFoundException if the parent task or subtask doesn't exist / isn't owned by the user.
     */
    @Transactional
    public SubtaskResponseDTO updateSubtask(Long taskId, Long subtaskId, SubtaskRequestDTO requestDTO) {
        assertTaskOwnedByCurrentUser(taskId);
        Subtask subtask = subtaskRepository.findByIdAndTaskId(subtaskId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with ID: " + subtaskId));
        subtaskMapper.updateEntityFromDTO(subtask, requestDTO);
        Subtask saved = subtaskRepository.save(subtask);
        return subtaskMapper.toResponseDTO(saved);
    }

    /**
     * Deletes a subtask belonging to a task owned by the authenticated user.
     *
     * @param taskId The parent task's ID.
     * @param subtaskId The subtask's ID.
     * @throws ResourceNotFoundException if the parent task or subtask doesn't exist / isn't owned by the user.
     */
    @Transactional
    public void deleteSubtask(Long taskId, Long subtaskId) {
        assertTaskOwnedByCurrentUser(taskId);
        Subtask subtask = subtaskRepository.findByIdAndTaskId(subtaskId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with ID: " + subtaskId));
        subtaskRepository.delete(subtask);
    }

    /**
     * Sets the completion state of every subtask under a task, used by TaskService to cascade
     * a task's own completion change onto its subtasks atomically. The caller is expected to
     * have already verified ownership of the parent task as part of that same operation.
     *
     * @param taskId The parent task's ID.
     * @param completed The new completion state.
     * @return The updated subtasks as response DTOs.
     */
    @Transactional
    public List<SubtaskResponseDTO> setCompletionForTask(Long taskId, boolean completed) {
        List<Subtask> subtasks = subtaskRepository.findByTaskIdOrderBySortOrderAsc(taskId);
        subtasks.forEach(subtask -> subtask.setCompleted(completed));
        List<Subtask> saved = subtaskRepository.saveAll(subtasks);
        return subtaskMapper.toResponseDTOList(saved);
    }
}
