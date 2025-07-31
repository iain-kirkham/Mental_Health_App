package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionCreationDTO;
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
     * @param creationDTO the data for the new session
     * @return the created session with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<PomodoroSessionResponseDTO> createPomodoroSession(@RequestBody @Valid PomodoroSessionCreationDTO creationDTO) {
        PomodoroSessionResponseDTO responseDTO = pomodoroSessionService.createPomodoroSession(creationDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /**
     * Retrieves all Pomodoro sessions.
     *
     * @return list of sessions with status 200 (OK), or 204 (No Content) if empty
     */
    @GetMapping
    public ResponseEntity<List<PomodoroSessionResponseDTO>> getAllPomodoroSessions() {
        List<PomodoroSessionResponseDTO> dtos = pomodoroSessionService.getAllPomodoroSessions();
        if (dtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    /**
     * Retrieves a Pomodoro session by its ID.
     *
     * @param id the ID of the session
     * @return the session with status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<PomodoroSessionResponseDTO> getPomodoroSessionById(@PathVariable Long id) {
        PomodoroSessionResponseDTO dto = pomodoroSessionService.getPomodoroSessionById(id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * Updates an existing Pomodoro session.
     *
     * @param id the ID of the session to update
     * @param updateDTO the updated session data
     * @return the updated session with status 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<PomodoroSessionResponseDTO> updatePomodoroSession(@PathVariable Long id, @RequestBody @Valid PomodoroSessionCreationDTO updateDTO) {
        PomodoroSessionResponseDTO responseDTO = pomodoroSessionService.updatePomodoroSession(id, updateDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
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
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
