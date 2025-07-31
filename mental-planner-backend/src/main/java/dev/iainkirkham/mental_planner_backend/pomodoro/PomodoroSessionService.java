package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionCreationDTO;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Service class for managing Pomodoro session entities.
 * Handles business logic for creating, retrieving, updating, and deleting sessions.
 */
@Service
public class PomodoroSessionService {

    private final PomodoroSessionRepository pomodoroSessionRepository;

    public PomodoroSessionService(PomodoroSessionRepository pomodoroSessionRepository) {
        this.pomodoroSessionRepository = pomodoroSessionRepository;
    }

    /**
     * Maps a PomodoroSessionCreationDTO to a PomodoroSession entity.
     * This prepares the DTO data for persistence.
     *
     * @param dto The DTO with input data from the client.
     * @return A new PomodoroSession entity.
     */
    private PomodoroSession toEntity(PomodoroSessionCreationDTO dto) {
        PomodoroSession entity = new PomodoroSession();
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setDuration(dto.getDuration());
        entity.setScore(dto.getScore());
        entity.setNotes(dto.getNotes());
        return entity;
    }

    /**
     * Converts a PomodoroSession entity into a PomodoroSessionResponseDTO.
     * This shapes internal data for safe exposure to the client.
     *
     * @param entity The session entity from the database.
     * @return A DTO suitable for client responses.
     */
    private PomodoroSessionResponseDTO toDto(PomodoroSession entity) {
        PomodoroSessionResponseDTO dto = new PomodoroSessionResponseDTO();
        dto.setId(entity.getId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setDuration(entity.getDuration());
        dto.setScore(entity.getScore());
        dto.setNotes(entity.getNotes());
        return dto;
    }

    /**
     * Creates a new Pomodoro session.
     * Converts the creation DTO to an entity, saves it, then returns a response DTO.
     *
     * @param creationDTO The DTO containing data to create a new session.
     * @return The created session as a response DTO.
     */
    public PomodoroSessionResponseDTO createPomodoroSession(PomodoroSessionCreationDTO creationDTO) {
        // TODO when adding multiple users fetch the entity first to perform ownership check
        PomodoroSession pomodoroSessionToSave = toEntity(creationDTO);
        PomodoroSession savedPomodoroSession = pomodoroSessionRepository.save(pomodoroSessionToSave);
        return toDto(savedPomodoroSession);
    }

    /**
     * Retrieves all Pomodoro sessions from the database.
     *
     * @return A list of response DTOs representing all sessions.
     */
    public List<PomodoroSessionResponseDTO> getAllPomodoroSessions() {
        List<PomodoroSession> pomodoroEntities = pomodoroSessionRepository.findAll();
        return pomodoroEntities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single Pomodoro session by its ID.
     *
     * @param id The ID of the session.
     * @return A response DTO of the found session.
     * @throws ResourceNotFoundException if the session doesn't exist.
     */
    public PomodoroSessionResponseDTO getPomodoroSessionById(Long id) {
        PomodoroSession pomodoroSession = pomodoroSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PomodoroSession not found with ID: " + id));
        // TODO when adding multiple users fetch the entity first to perform ownership check
        return toDto(pomodoroSession);
    }

    /**
     * Updates an existing Pomodoro session.
     *
     * @param id The ID of the session to update.
     * @param updateDTO The DTO containing updated session data.
     * @return A response DTO of the updated session.
     * @throws ResourceNotFoundException if the session doesn't exist.
     */
    public PomodoroSessionResponseDTO updatePomodoroSession(Long id, PomodoroSessionCreationDTO updateDTO) {
        PomodoroSession existingPomodoroSession = pomodoroSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PomodoroSession not found with ID: " + id));
        // TODO when adding multiple users fetch the entity first to perform ownership check

        existingPomodoroSession.setStartTime(updateDTO.getStartTime());
        existingPomodoroSession.setEndTime(updateDTO.getEndTime());
        existingPomodoroSession.setDuration(updateDTO.getDuration());
        existingPomodoroSession.setScore(updateDTO.getScore());
        existingPomodoroSession.setNotes(updateDTO.getNotes());

        PomodoroSession updatedPomodoroSession = pomodoroSessionRepository.save(existingPomodoroSession);
        return toDto(updatedPomodoroSession);
    }

    /**
     * Deletes a Pomodoro session by ID.
     *
     * @param id The ID of the session to delete.
     * @throws ResourceNotFoundException if the session doesn't exist.
     */
    public void deletePomodoroSession(Long id) {
        // TODO when adding multiple users fetch the entity first to perform ownership check
        if (!pomodoroSessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("PomodoroSession not found with ID: " + id);
        }
        pomodoroSessionRepository.deleteById(id);
    }
}