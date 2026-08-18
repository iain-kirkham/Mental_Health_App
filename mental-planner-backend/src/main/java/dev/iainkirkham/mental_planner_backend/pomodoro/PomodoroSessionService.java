package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import dev.iainkirkham.mental_planner_backend.config.OwnedEntityLookup;
import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionRequestDTO;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO;
import dev.iainkirkham.mental_planner_backend.tasks.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service class for managing Pomodoro session entities.
 * Handles business logic for creating, retrieving, updating, and deleting sessions.
 * All operations are filtered by the authenticated user to ensure data isolation.
 */
@Service
@Transactional(readOnly = true)
public class PomodoroSessionService {

    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final AuthenticationContext authenticationContext;
    private final OwnedEntityLookup ownedEntityLookup;
    private final PomodoroSessionMapper pomodoroSessionMapper;
    private final TaskService taskService;

    public PomodoroSessionService(PomodoroSessionRepository pomodoroSessionRepository,
                                 AuthenticationContext authenticationContext,
                                 OwnedEntityLookup ownedEntityLookup,
                                 PomodoroSessionMapper pomodoroSessionMapper,
                                 TaskService taskService) {
        this.pomodoroSessionRepository = pomodoroSessionRepository;
        this.authenticationContext = authenticationContext;
        this.ownedEntityLookup = ownedEntityLookup;
        this.pomodoroSessionMapper = pomodoroSessionMapper;
        this.taskService = taskService;
    }

    /**
     * Verifies a session's linked task (if any) belongs to the authenticated user.
     *
     * @throws ResourceNotFoundException if taskId is set but doesn't belong to the user.
     */
    private void assertTaskOwnedIfPresent(PomodoroSessionRequestDTO requestDTO) {
        if (requestDTO.getTaskId() != null) {
            taskService.assertOwnedByCurrentUser(requestDTO.getTaskId());
        }
    }

    /**
     * Creates a new Pomodoro session for the authenticated user.
     *
     * @param requestDTO The session DTO to create.
     * @return The saved session as a response DTO.
     */
    @Transactional
    public PomodoroSessionResponseDTO createPomodoroSession(PomodoroSessionRequestDTO requestDTO) {
        assertTaskOwnedIfPresent(requestDTO);
        PomodoroSession pomodoroSession = pomodoroSessionMapper.toEntity(requestDTO);
        pomodoroSession.setId(null); // Ensure ID is null for new entries
        // Automatically set userId from authenticated user
        pomodoroSession.setUserId(authenticationContext.getCurrentUserId());
        PomodoroSession savedSession = pomodoroSessionRepository.save(pomodoroSession);
        return pomodoroSessionMapper.toResponseDTO(savedSession);
    }

    /**
     * Retrieves all Pomodoro sessions for the authenticated user, ordered by start time descending.
     *
     * @return A list of all sessions as response DTOs belonging to the current user.
     */
    public List<PomodoroSessionResponseDTO> getAllPomodoroSessions() {
        String userId = authenticationContext.getCurrentUserId();
        List<PomodoroSession> sessions = pomodoroSessionRepository.findByUserIdOrderByStartTimeDesc(userId);
        return pomodoroSessionMapper.toResponseDTOList(sessions);
    }

    /**
     * Retrieves Pomodoro sessions for the authenticated user within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return A list of sessions as response DTOs within the date range, ordered by start time descending.
     */
    public List<PomodoroSessionResponseDTO> getPomodoroSessionsByDateRange(java.time.Instant startDate, java.time.Instant endDate) {
        String userId = authenticationContext.getCurrentUserId();
        List<PomodoroSession> sessions = pomodoroSessionRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(
            userId, startDate, endDate
        );
        return pomodoroSessionMapper.toResponseDTOList(sessions);
    }

    /**
     * Retrieves a single Pomodoro session by its ID if it belongs to the authenticated user.
     *
     * @param id The ID of the session.
     * @return The found session as a response DTO.
     * @throws ResourceNotFoundException if the session doesn't exist or doesn't belong to the user.
     */
    public PomodoroSessionResponseDTO getPomodoroSessionById(Long id) {
        PomodoroSession session = findOwnedSession(id);
        return pomodoroSessionMapper.toResponseDTO(session);
    }

    /**
     * Looks up a Pomodoro session by ID, verifying it belongs to the authenticated user.
     */
    private PomodoroSession findOwnedSession(Long id) {
        return ownedEntityLookup.findOwnedOrThrow(pomodoroSessionRepository::findByIdAndUserId, id, "PomodoroSession");
    }

    /**
     * Updates an existing Pomodoro session if it belongs to the authenticated user.
     *
     * @param id The ID of the session to update.
     * @param requestDTO The DTO with updated data.
     * @return The updated session as a response DTO.
     * @throws ResourceNotFoundException if the session doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public PomodoroSessionResponseDTO updatePomodoroSession(Long id, PomodoroSessionRequestDTO requestDTO) {
        assertTaskOwnedIfPresent(requestDTO);

        PomodoroSession existingPomodoroSession = findOwnedSession(id);

        pomodoroSessionMapper.updateEntityFromDTO(existingPomodoroSession, requestDTO);

        PomodoroSession updatedSession = pomodoroSessionRepository.save(existingPomodoroSession);
        return pomodoroSessionMapper.toResponseDTO(updatedSession);
    }

    /**
     * Deletes a Pomodoro session by ID if it belongs to the authenticated user.
     *
     * @param id The ID of the session to delete.
     * @throws ResourceNotFoundException if the session doesn't exist or doesn't belong to the user.
     */
    @Transactional
    public void deletePomodoroSession(Long id) {
        pomodoroSessionRepository.delete(findOwnedSession(id));
    }
}