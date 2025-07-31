package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryCreationDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoodEntryControllerTest {

    @Mock
    private MoodEntryService moodEntryService;

    @InjectMocks
    private MoodEntryController moodEntryController;

    private MoodEntry testMoodEntry;

    // prepare DTO for testing
    private MoodEntryCreationDTO testCreationDTO;
    private MoodEntryResponseDTO testResponseDTO;
    private MoodEntryResponseDTO updatedResponseDTO;

    @BeforeEach
    void setUp() {
        // Creation DTO
        testCreationDTO = new MoodEntryCreationDTO();
        testCreationDTO.setMoodScore((short) 4);
        testCreationDTO.setDateTime(Instant.now());
        testCreationDTO.setFactors(Arrays.asList("Work", "Stress"));
        testCreationDTO.setNotes("Had a tough day.");

        // Setup for Response DTO with a standard response
        testResponseDTO = new MoodEntryResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setMoodScore((short) 4);
        testResponseDTO.setDateTime(Instant.now());
        testResponseDTO.setFactors(Arrays.asList("Work", "Stress"));
        testResponseDTO.setNotes("Had a tough day.");

        // Setup for updated Response DTO
        updatedResponseDTO = new MoodEntryResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setMoodScore((short) 5);
        updatedResponseDTO.setDateTime(Instant.now().plusSeconds(10));
        updatedResponseDTO.setFactors(Arrays.asList("Achievement", "Relaxation"));
        updatedResponseDTO.setNotes("Feeling fantastic after finishing the task!");
    }

    @Test
    void createMoodEntry_ShouldReturnCreatedMoodEntry() {
        // Arrange: Service's create method returns a MoodEntryResponseDTO
        when(moodEntryService.createMoodEntry(any(MoodEntryCreationDTO.class))).thenReturn(testResponseDTO);

        // Act
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.createMoodEntry(testCreationDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testResponseDTO.getId());
        assertThat(response.getBody().getMoodScore()).isEqualTo(testResponseDTO.getMoodScore());
        assertThat(response.getBody().getNotes()).isEqualTo(testResponseDTO.getNotes());
        // Verify that the service method was called
        verify(moodEntryService, times(1)).createMoodEntry(testCreationDTO);
    }

    @Test
    void getAllMoodEntries_ShouldReturnAllMoodEntries() {
        // Arrange: Service's getAll method returns a list of MoodEntryResponseDTOs
        List<MoodEntryResponseDTO> expectedDtos = Arrays.asList(testResponseDTO, new MoodEntryResponseDTO()); // Add another dummy DTO
        when(moodEntryService.getAllMoodEntries()).thenReturn(expectedDtos);

        // Act
        ResponseEntity<List<MoodEntryResponseDTO>> response = moodEntryController.getAllMoodEntries();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getId()).isEqualTo(testResponseDTO.getId());
        verify(moodEntryService, times(1)).getAllMoodEntries();
    }

    @Test
    void getAllMoodEntries_ShouldReturnNoContentWhenEmpty() {
        // Arrange: Service returns an empty list
        when(moodEntryService.getAllMoodEntries()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<MoodEntryResponseDTO>> response = moodEntryController.getAllMoodEntries();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull(); // Body should be null for NO_CONTENT
        verify(moodEntryService, times(1)).getAllMoodEntries();
    }

    @Test
    void getMoodEntryById_ShouldReturnMoodEntryWhenFound() {
        // Arrange: Service's getById method returns a MoodEntryResponseDTO
        when(moodEntryService.getMoodEntryById(anyLong())).thenReturn(testResponseDTO);

        // Act
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.getMoodEntryById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testResponseDTO.getId());
        verify(moodEntryService, times(1)).getMoodEntryById(1L);
    }

    @Test
    void getMoodEntryById_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Service throws ResourceNotFoundException
        when(moodEntryService.getMoodEntryById(anyLong())).thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert: Expect the controller to propagate the 404 status via @ResponseStatus
        // Spring's default exception handling will convert ResourceNotFoundException to 404
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.getMoodEntryById(99L));
        verify(moodEntryService, times(1)).getMoodEntryById(99L);
    }

    @Test
    void updateMoodEntry_ShouldReturnUpdatedMoodEntryWhenFound() {
        // Arrange: Service's update method returns the updated MoodEntryResponseDTO
        when(moodEntryService.updateMoodEntry(anyLong(), any(MoodEntryCreationDTO.class))).thenReturn(updatedResponseDTO);

        // Act
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.updateMoodEntry(1L, testCreationDTO); // Using testCreationDTO as update input

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(updatedResponseDTO.getId());
        assertThat(response.getBody().getMoodScore()).isEqualTo(updatedResponseDTO.getMoodScore());
        assertThat(response.getBody().getNotes()).isEqualTo(updatedResponseDTO.getNotes());
        verify(moodEntryService, times(1)).updateMoodEntry(1L, testCreationDTO);
    }

    @Test
    void updateMoodEntry_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Service throws ResourceNotFoundException for update
        when(moodEntryService.updateMoodEntry(anyLong(), any(MoodEntryCreationDTO.class)))
                .thenThrow(new ResourceNotFoundException("Not found for update"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.updateMoodEntry(99L, testCreationDTO));
        verify(moodEntryService, times(1)).updateMoodEntry(99L, testCreationDTO);
    }

    @Test
    void deleteMoodEntry_ShouldReturnNoContentWhenFound() {
        // Arrange: Service's delete method returns void
        doNothing().when(moodEntryService).deleteMoodEntry(anyLong());

        // Act
        ResponseEntity<Void> response = moodEntryController.deleteMoodEntry(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(moodEntryService, times(1)).deleteMoodEntry(1L);
    }

    @Test
    void deleteMoodEntry_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Service throws ResourceNotFoundException for delete
        doThrow(new ResourceNotFoundException("Not found for delete")).when(moodEntryService).deleteMoodEntry(anyLong());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.deleteMoodEntry(99L));
        verify(moodEntryService, times(1)).deleteMoodEntry(99L);
    }
}