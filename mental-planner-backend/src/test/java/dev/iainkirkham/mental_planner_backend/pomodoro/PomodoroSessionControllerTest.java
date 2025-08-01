package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionCreationDTO;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroSessionControllerTest {
    @Mock
    private PomodoroSessionService pomodoroSessionService;

    @InjectMocks
    private PomodoroSessionController pomodoroSessionController;

    // Test data objects - recreated before each test for isolation
    private PomodoroSessionCreationDTO testCreationDTO;
    private PomodoroSessionResponseDTO testResponseDTO;
    private PomodoroSessionCreationDTO updateCreationDTO;
    private PomodoroSessionResponseDTO updatedResponseDTO;

    /**
     * Sets up test data before each test method.
     *
     * Creates realistic test objects representing:
     * - Client input (CreationDTO) - what would come from frontend
     * - Service responses (ResponseDTO) - what service layer returns
     * - Update scenarios with modified values
     *
     * Using fresh objects per test prevents test pollution and ensures
     * each test starts with a clean state.
     */
    @BeforeEach
    void setUp() {
        // Standard 25-minute pomodoro session input from client
        testCreationDTO = new PomodoroSessionCreationDTO();
        testCreationDTO.setStartTime(Instant.now());
        testCreationDTO.setEndTime(Instant.now().plusSeconds(25 * 60));
        testCreationDTO.setDuration(25);
        testCreationDTO.setScore((short) 4);
        testCreationDTO.setNotes("Focused work session.");

        // Expected service response after successful creation/retrieval
        testResponseDTO = new PomodoroSessionResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setStartTime(testCreationDTO.getStartTime());
        testResponseDTO.setEndTime(testCreationDTO.getEndTime());
        testResponseDTO.setDuration(testCreationDTO.getDuration());
        testResponseDTO.setScore(testCreationDTO.getScore());
        testResponseDTO.setNotes(testCreationDTO.getNotes());

        // Modified data for update operations testing
        updateCreationDTO = new PomodoroSessionCreationDTO();
        updateCreationDTO.setStartTime(Instant.now().minusSeconds(10)); // Slight change
        updateCreationDTO.setEndTime(Instant.now().plusSeconds(30 * 60)); // Longer duration
        updateCreationDTO.setDuration(30);
        updateCreationDTO.setScore((short) 5); // Improved score
        updateCreationDTO.setNotes("Even more focused work session, extended!");

        // Expected service response after successful update
        updatedResponseDTO = new PomodoroSessionResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setStartTime(updateCreationDTO.getStartTime());
        updatedResponseDTO.setEndTime(updateCreationDTO.getEndTime());
        updatedResponseDTO.setDuration(updateCreationDTO.getDuration());
        updatedResponseDTO.setScore(updateCreationDTO.getScore());
        updatedResponseDTO.setNotes(updateCreationDTO.getNotes());
    }

    @Test
    void createPomodoroSession_ShouldReturnCreatedSession() {
        // Arrange: Mock service to return successful creation response
        when(pomodoroSessionService.createPomodoroSession(any(PomodoroSessionCreationDTO.class)))
                .thenReturn(testResponseDTO);

        // Act: Call controller endpoint
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.createPomodoroSession(testCreationDTO);

        // Assert: Verify HTTP contract and data integrity
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testResponseDTO.getId());
        assertThat(response.getBody().getDuration()).isEqualTo(testResponseDTO.getDuration());
        assertThat(response.getBody().getNotes()).isEqualTo(testResponseDTO.getNotes());

        // Verify service interaction
        verify(pomodoroSessionService, times(1)).createPomodoroSession(testCreationDTO);
    }

    @Test
    void getAllPomodoroSessions_ShouldReturnAllSessions() {
        // Arrange: Mock service to return multiple session
        List<PomodoroSessionResponseDTO> expectedDtos = Arrays.asList(testResponseDTO, new PomodoroSessionResponseDTO());
        when(pomodoroSessionService.getAllPomodoroSessions()).thenReturn(expectedDtos);

        // Act: Call controller endpoint
        ResponseEntity<List<PomodoroSessionResponseDTO>> response = pomodoroSessionController.getAllPomodoroSessions();

        // Assert: Verify response structure and content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(pomodoroSessionService, times(1)).getAllPomodoroSessions();
    }

    @Test
    void getAllPomodoroSessions_ShouldReturnNoContentWhenEmpty() {
        // Arrange: Mock service to return empty list
        when(pomodoroSessionService.getAllPomodoroSessions()).thenReturn(Collections.emptyList());

        // Act: Call controller endpoint
        ResponseEntity<List<PomodoroSessionResponseDTO>> response = pomodoroSessionController.getAllPomodoroSessions();

        // Assert: Verify empty response handling
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(pomodoroSessionService, times(1)).getAllPomodoroSessions();
    }

    @Test
    void getPomodoroSessionById_ShouldReturnSessionWhenFound() {
        // Arrange: Mock service to return specific session
        when(pomodoroSessionService.getPomodoroSessionById(anyLong())).thenReturn(testResponseDTO);

        // Act: Call controller endpoint with specific ID
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.getPomodoroSessionById(1L);

        // Assert: Verify successful retrieval
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testResponseDTO.getId());
        verify(pomodoroSessionService, times(1)).getPomodoroSessionById(1L);
    }

    @Test
    void getPomodoroSessionById_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        when(pomodoroSessionService.getPomodoroSessionById(anyLong())).thenThrow(new ResourceNotFoundException("Session not found"));

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> pomodoroSessionController.getPomodoroSessionById(99L));
        verify(pomodoroSessionService, times(1)).getPomodoroSessionById(99L);
    }

    @Test
    void updatePomodoroSession_ShouldReturnUpdatedSessionWhenFound() {
        // Arrange: Mock service to return updated session
        when(pomodoroSessionService.updatePomodoroSession(anyLong(), any(PomodoroSessionCreationDTO.class)))
                .thenReturn(updatedResponseDTO);

        // Act: Call controller update endpoint
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.updatePomodoroSession(1L, updateCreationDTO);

        // Assert: Verify update success and data changes
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(updatedResponseDTO.getId());
        assertThat(response.getBody().getDuration()).isEqualTo(updatedResponseDTO.getDuration());
        assertThat(response.getBody().getNotes()).isEqualTo(updatedResponseDTO.getNotes());
        assertThat(response.getBody().getScore()).isEqualTo(updatedResponseDTO.getScore());
        verify(pomodoroSessionService, times(1)).updatePomodoroSession(1L, updateCreationDTO);
    }

    @Test
    void updatePomodoroSession_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        when(pomodoroSessionService.updatePomodoroSession(anyLong(), any(PomodoroSessionCreationDTO.class)))
                .thenThrow(new ResourceNotFoundException("Session not found for update"));

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> pomodoroSessionController.updatePomodoroSession(99L, updateCreationDTO));
        verify(pomodoroSessionService, times(1)).updatePomodoroSession(99L, updateCreationDTO);
    }

    @Test
    void deletePomodoroSession_ShouldReturnNoContentWhenFound() {
        // Arrange: Mock service to complete deletion without error
        doNothing().when(pomodoroSessionService).deletePomodoroSession(anyLong());

        // Act: Call controller delete endpoint
        ResponseEntity<Void> response = pomodoroSessionController.deletePomodoroSession(1L);

        // Assert: Verify successful deletion response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(pomodoroSessionService, times(1)).deletePomodoroSession(1L);
    }

    @Test
    void deletePomodoroSession_ShouldReturnNotFoundWhenNotFound() {
        // Arrange
        doThrow(new ResourceNotFoundException("Session not found for delete")).when(pomodoroSessionService).deletePomodoroSession(anyLong());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pomodoroSessionController.deletePomodoroSession(99L));
        verify(pomodoroSessionService, times(1)).deletePomodoroSession(99L);
    }
}