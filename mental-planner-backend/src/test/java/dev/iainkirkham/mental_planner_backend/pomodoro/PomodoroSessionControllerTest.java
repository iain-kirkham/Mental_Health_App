package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionRequestDTO;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PomodoroSessionControllerTest {
    @Mock
    private PomodoroSessionService pomodoroSessionService;

    @InjectMocks
    private PomodoroSessionController pomodoroSessionController;

    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T00:00:00Z");

    // Test data objects - recreated before each test for isolation
    private PomodoroSessionRequestDTO testRequestDTO;
    private PomodoroSessionResponseDTO savedResponseDTO;
    private PomodoroSessionRequestDTO updateRequestDTO;
    private PomodoroSessionResponseDTO updatedResponseDTO;

    // Test userId for consistent authentication context (not exposed in DTOs)
    private static final String TEST_USER_ID = "user_test123";

    /**
     * Sets up test data before each test method.
     * Creates realistic test objects representing:
     * - Client request DTOs - what would come from frontend (no userId/ID)
     * - Service response DTOs - what service layer returns
     * - Update scenarios with modified values
     * Using fresh objects per test prevents test pollution and ensures
     * each test starts with a clean state.
     *
     * Note: userId is automatically set by the service layer via AuthenticationContext,
     * and is NOT exposed in DTOs for security.
     */
    @BeforeEach
    void setUp() {
        // Standard 25-minute pomodoro session input from client
        testRequestDTO = new PomodoroSessionRequestDTO();
        testRequestDTO.setStartTime(FIXED_NOW);
        testRequestDTO.setEndTime(FIXED_NOW.plusSeconds(25 * 60));
        testRequestDTO.setDuration(25);
        testRequestDTO.setScore((short) 4);
        testRequestDTO.setNotes("Focused work session.");

        // Expected service response after successful creation/retrieval
        savedResponseDTO = new PomodoroSessionResponseDTO();
        savedResponseDTO.setId(1L);
        savedResponseDTO.setStartTime(FIXED_NOW);
        savedResponseDTO.setEndTime(FIXED_NOW.plusSeconds(25 * 60));
        savedResponseDTO.setDuration(25);
        savedResponseDTO.setScore((short) 4);
        savedResponseDTO.setNotes("Focused work session.");

        // Modified data for update operations testing
        updateRequestDTO = new PomodoroSessionRequestDTO();
        updateRequestDTO.setStartTime(FIXED_NOW.minusSeconds(10)); // Slight change
        updateRequestDTO.setEndTime(FIXED_NOW.plusSeconds(30 * 60)); // Longer duration
        updateRequestDTO.setDuration(30);
        updateRequestDTO.setScore((short) 5); // Improved score
        updateRequestDTO.setNotes("Even more focused work session, extended!");

        // Expected service response after successful update
        updatedResponseDTO = new PomodoroSessionResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setStartTime(FIXED_NOW.minusSeconds(10));
        updatedResponseDTO.setEndTime(FIXED_NOW.plusSeconds(30 * 60));
        updatedResponseDTO.setDuration(30);
        updatedResponseDTO.setScore((short) 5);
        updatedResponseDTO.setNotes("Even more focused work session, extended!");
    }

    @Test
    void createPomodoroSession_ShouldReturnCreatedSession() {
        // Arrange: Mock service to return successful creation response
        when(pomodoroSessionService.createPomodoroSession(any(PomodoroSessionRequestDTO.class)))
                .thenReturn(savedResponseDTO);

        // Act: Call controller endpoint
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.createPomodoroSession(testRequestDTO);

        // Assert: Verify HTTP contract and data integrity
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(pomodoroSessionService).createPomodoroSession(testRequestDTO);
    }

    @Test
    void getAllPomodoroSessions_ShouldReturnAllSessions() {
        // Arrange: Mock service to return multiple sessions
        PomodoroSessionResponseDTO secondSession = new PomodoroSessionResponseDTO();
        secondSession.setId(2L);
        secondSession.setStartTime(FIXED_NOW.plusSeconds(3600));
        secondSession.setDuration(25);
        List<PomodoroSessionResponseDTO> expectedSessions = List.of(savedResponseDTO, secondSession);
        when(pomodoroSessionService.getAllPomodoroSessions()).thenReturn(expectedSessions);

        // Act: Call controller endpoint (with null dates = get all)
        ResponseEntity<List<PomodoroSessionResponseDTO>> response = pomodoroSessionController.getAllPomodoroSessions(null, null);

        // Assert: Verify response structure and content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2);
        verify(pomodoroSessionService).getAllPomodoroSessions();
    }

    @Test
    void getAllPomodoroSessions_ShouldReturnNoContentWhenEmpty() {
        // Arrange: Mock service to return empty list
        when(pomodoroSessionService.getAllPomodoroSessions()).thenReturn(List.of());

        // Act: Call controller endpoint
        ResponseEntity<List<PomodoroSessionResponseDTO>> response = pomodoroSessionController.getAllPomodoroSessions(null, null);

        // Assert: Verify empty response handling
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(pomodoroSessionService).getAllPomodoroSessions();
    }

    @Test
    void getPomodoroSessionById_ShouldReturnSessionWhenFound() {
        // Arrange: Mock service to return specific session
        when(pomodoroSessionService.getPomodoroSessionById(anyLong())).thenReturn(savedResponseDTO);

        // Act: Call controller endpoint with specific ID
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.getPomodoroSessionById(1L);

        // Assert: Verify successful retrieval
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(pomodoroSessionService).getPomodoroSessionById(1L);
    }

    @Test
    void getPomodoroSessionById_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        when(pomodoroSessionService.getPomodoroSessionById(anyLong())).thenThrow(new ResourceNotFoundException("Session not found"));

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> pomodoroSessionController.getPomodoroSessionById(99L));
        verify(pomodoroSessionService).getPomodoroSessionById(99L);
    }

    @Test
    void updatePomodoroSession_ShouldReturnUpdatedSessionWhenFound() {
        // Arrange: Mock service to return updated session
        when(pomodoroSessionService.updatePomodoroSession(anyLong(), any(PomodoroSessionRequestDTO.class)))
                .thenReturn(updatedResponseDTO);

        // Act: Call controller update endpoint
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.updatePomodoroSession(1L, updateRequestDTO);

        // Assert: Verify update success and data changes
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedResponseDTO);
        verify(pomodoroSessionService).updatePomodoroSession(1L, updateRequestDTO);
    }

    @Test
    void updatePomodoroSession_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        when(pomodoroSessionService.updatePomodoroSession(anyLong(), any(PomodoroSessionRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Session not found for update"));

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> pomodoroSessionController.updatePomodoroSession(99L, updateRequestDTO));
        verify(pomodoroSessionService).updatePomodoroSession(99L, updateRequestDTO);
    }

    @Test
    void deletePomodoroSession_ShouldReturnNoContentWhenFound() {
        // Arrange: Mock service to complete deletion without error
        doNothing().when(pomodoroSessionService).deletePomodoroSession(anyLong());

        // Act: Call controller delete endpoint
        ResponseEntity<Void> response = pomodoroSessionController.deletePomodoroSession(1L);

        // Assert: Verify successful deletion response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(pomodoroSessionService).deletePomodoroSession(1L);
    }

    @Test
    void deletePomodoroSession_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        doThrow(new ResourceNotFoundException("Session not found for delete")).when(pomodoroSessionService).deletePomodoroSession(anyLong());

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> pomodoroSessionController.deletePomodoroSession(99L));
        verify(pomodoroSessionService).deletePomodoroSession(99L);
    }
}