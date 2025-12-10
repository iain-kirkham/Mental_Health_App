package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionRequestDTO;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing pomodoro sessions.
 * Provides entry points for creation, retrieval, updating, and deletion (CRUD) of Pomodoro session records.
 */
@RestController
@RequestMapping("api/pomodoro")
public class PomodoroSessionController {

    private final PomodoroSessionService pomodoroSessionService;

    public PomodoroSessionController(PomodoroSessionService pomodoroSessionService) {
        this.pomodoroSessionService = pomodoroSessionService;
    }

    /**
     * Creates a new Pomodoro session.
     *
     * @param requestDTO the data for the new session
     * @return the created session with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<PomodoroSessionResponseDTO> createPomodoroSession(@RequestBody @Valid PomodoroSessionRequestDTO requestDTO) {
        PomodoroSessionResponseDTO savedSession = pomodoroSessionService.createPomodoroSession(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSession);
    }

    /**
     * Retrieves all Pomodoro sessions, optionally filtered by date range.
     *
     * @param startDate optional start date for filtering (ISO-8601 format)
     * @param endDate optional end date for filtering (ISO-8601 format)
     * @return list of sessions with status 200 (OK), or 204 (No Content) if empty
     */
    @GetMapping
    public ResponseEntity<List<PomodoroSessionResponseDTO>> getAllPomodoroSessions(
            @RequestParam(required = false) java.time.Instant startDate,
            @RequestParam(required = false) java.time.Instant endDate) {

        List<PomodoroSessionResponseDTO> sessions;

        if (startDate != null && endDate != null) {
            sessions = pomodoroSessionService.getPomodoroSessionsByDateRange(startDate, endDate);
        } else {
            sessions = pomodoroSessionService.getAllPomodoroSessions();
        }

        if (sessions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sessions);
    }

    /**
     * Retrieves a Pomodoro session by its ID.
     *
     * @param id the ID of the session
     * @return the session with status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<PomodoroSessionResponseDTO> getPomodoroSessionById(@PathVariable Long id) {
        PomodoroSessionResponseDTO session = pomodoroSessionService.getPomodoroSessionById(id);
        return ResponseEntity.ok(session);
    }

    /**
     * Updates an existing Pomodoro session.
     *
     * @param id the ID of the session to update
     * @param requestDTO the updated session data
     * @return the updated session with status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<PomodoroSessionResponseDTO> updatePomodoroSession(@PathVariable Long id, @RequestBody @Valid PomodoroSessionRequestDTO requestDTO) {
        PomodoroSessionResponseDTO updated = pomodoroSessionService.updatePomodoroSession(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a Pomodoro session by its ID.
     *
     * @param id the ID of the session to delete
     * @return status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePomodoroSession(@PathVariable Long id) {
        pomodoroSessionService.deletePomodoroSession(id);
        return ResponseEntity.noContent().build();
    }
}
