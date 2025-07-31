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
    @Mock // Now mock the PomodoroSessionService
    private PomodoroSessionService pomodoroSessionService;

    @InjectMocks
    private PomodoroSessionController pomodoroSessionController;

    // DTOs for testing
    private PomodoroSessionCreationDTO testCreationDTO;
    private PomodoroSessionResponseDTO testResponseDTO;
    private PomodoroSessionCreationDTO updateCreationDTO; // For update requests
    private PomodoroSessionResponseDTO updatedResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup for Creation DTO
        testCreationDTO = new PomodoroSessionCreationDTO();
        testCreationDTO.setStartTime(Instant.now());
        testCreationDTO.setEndTime(Instant.now().plusSeconds(25 * 60));
        testCreationDTO.setDuration(25);
        testCreationDTO.setScore((short) 4);
        testCreationDTO.setNotes("Focused work session.");

        // Setup for Response DTO (what the service would return after creation/fetch)
        testResponseDTO = new PomodoroSessionResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setStartTime(testCreationDTO.getStartTime());
        testResponseDTO.setEndTime(testCreationDTO.getEndTime());
        testResponseDTO.setDuration(testCreationDTO.getDuration());
        testResponseDTO.setScore(testCreationDTO.getScore());
        testResponseDTO.setNotes(testCreationDTO.getNotes());

        // Setup for Update Creation DTO (what the client sends for update)
        updateCreationDTO = new PomodoroSessionCreationDTO();
        updateCreationDTO.setStartTime(Instant.now().minusSeconds(10)); // Slight change
        updateCreationDTO.setEndTime(Instant.now().plusSeconds(30 * 60)); // Longer duration
        updateCreationDTO.setDuration(30);
        updateCreationDTO.setScore((short) 5); // Improved score
        updateCreationDTO.setNotes("Even more focused work session, extended!");

        // Setup for Updated Response DTO (what the service would return after update)
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
        // Arrange
        when(pomodoroSessionService.createPomodoroSession(any(PomodoroSessionCreationDTO.class)))
                .thenReturn(testResponseDTO);

        // Act
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.createPomodoroSession(testCreationDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testResponseDTO.getId());
        assertThat(response.getBody().getDuration()).isEqualTo(testResponseDTO.getDuration());
        assertThat(response.getBody().getNotes()).isEqualTo(testResponseDTO.getNotes());
        verify(pomodoroSessionService, times(1)).createPomodoroSession(testCreationDTO);
    }

    @Test
    void getAllPomodoroSessions_ShouldReturnAllSessions() {
        // Arrange
        List<PomodoroSessionResponseDTO> expectedDtos = Arrays.asList(testResponseDTO, new PomodoroSessionResponseDTO());
        when(pomodoroSessionService.getAllPomodoroSessions()).thenReturn(expectedDtos);

        // Act
        ResponseEntity<List<PomodoroSessionResponseDTO>> response = pomodoroSessionController.getAllPomodoroSessions();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(pomodoroSessionService, times(1)).getAllPomodoroSessions();
    }

    @Test
    void getAllPomodoroSessions_ShouldReturnNoContentWhenEmpty() {
        // Arrange
        when(pomodoroSessionService.getAllPomodoroSessions()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<PomodoroSessionResponseDTO>> response = pomodoroSessionController.getAllPomodoroSessions();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(pomodoroSessionService, times(1)).getAllPomodoroSessions();
    }

    @Test
    void getPomodoroSessionById_ShouldReturnSessionWhenFound() {
        // Arrange
        when(pomodoroSessionService.getPomodoroSessionById(anyLong())).thenReturn(testResponseDTO);

        // Act
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.getPomodoroSessionById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testResponseDTO.getId());
        verify(pomodoroSessionService, times(1)).getPomodoroSessionById(1L);
    }

    @Test
    void getPomodoroSessionById_ShouldReturnNotFoundWhenNotFound() {
        // Arrange
        when(pomodoroSessionService.getPomodoroSessionById(anyLong())).thenThrow(new ResourceNotFoundException("Session not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pomodoroSessionController.getPomodoroSessionById(99L));
        verify(pomodoroSessionService, times(1)).getPomodoroSessionById(99L);
    }

    @Test
    void updatePomodoroSession_ShouldReturnUpdatedSessionWhenFound() {
        // Arrange
        when(pomodoroSessionService.updatePomodoroSession(anyLong(), any(PomodoroSessionCreationDTO.class)))
                .thenReturn(updatedResponseDTO);

        // Act
        ResponseEntity<PomodoroSessionResponseDTO> response = pomodoroSessionController.updatePomodoroSession(1L, updateCreationDTO);

        // Assert
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
        // Arrange
        when(pomodoroSessionService.updatePomodoroSession(anyLong(), any(PomodoroSessionCreationDTO.class)))
                .thenThrow(new ResourceNotFoundException("Session not found for update"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pomodoroSessionController.updatePomodoroSession(99L, updateCreationDTO));
        verify(pomodoroSessionService, times(1)).updatePomodoroSession(99L, updateCreationDTO);
    }

    @Test
    void deletePomodoroSession_ShouldReturnNoContentWhenFound() {
        // Arrange
        doNothing().when(pomodoroSessionService).deletePomodoroSession(anyLong());

        // Act
        ResponseEntity<Void> response = pomodoroSessionController.deletePomodoroSession(1L);

        // Assert
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