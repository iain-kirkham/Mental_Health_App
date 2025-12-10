package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionRequestDTO;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Service class for managing Pomodoro session entities.
 * Handles business logic for creating, retrieving, updating, and deleting sessions.
 * All operations are filtered by the authenticated user to ensure data isolation.
 */
@Service
public class PomodoroSessionService {

    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final AuthenticationContext authenticationContext;
    private final PomodoroSessionMapper pomodoroSessionMapper;

    public PomodoroSessionService(PomodoroSessionRepository pomodoroSessionRepository,
                                 AuthenticationContext authenticationContext,
                                 PomodoroSessionMapper pomodoroSessionMapper) {
        this.pomodoroSessionRepository = pomodoroSessionRepository;
        this.authenticationContext = authenticationContext;
        this.pomodoroSessionMapper = pomodoroSessionMapper;
    }

    /**
     * Creates a new Pomodoro session for the authenticated user.
     *
     * @param requestDTO The session DTO to create.
     * @return The saved session as a response DTO.
     */
    public PomodoroSessionResponseDTO createPomodoroSession(PomodoroSessionRequestDTO requestDTO) {
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
        String userId = authenticationContext.getCurrentUserId();
        PomodoroSession session = pomodoroSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("PomodoroSession not found with ID: " + id));
        return pomodoroSessionMapper.toResponseDTO(session);
    }

    /**
     * Updates an existing Pomodoro session if it belongs to the authenticated user.
     *
     * @param id The ID of the session to update.
     * @param requestDTO The DTO with updated data.
     * @return The updated session as a response DTO.
     * @throws ResourceNotFoundException if the session doesn't exist or doesn't belong to the user.
     */
    public PomodoroSessionResponseDTO updatePomodoroSession(Long id, PomodoroSessionRequestDTO requestDTO) {
        String userId = authenticationContext.getCurrentUserId();
        PomodoroSession existingPomodoroSession = pomodoroSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("PomodoroSession not found with ID: " + id));

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
    public void deletePomodoroSession(Long id) {
        String userId = authenticationContext.getCurrentUserId();
        PomodoroSession session = pomodoroSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("PomodoroSession not found with ID: " + id));
        pomodoroSessionRepository.delete(session);
    }
}