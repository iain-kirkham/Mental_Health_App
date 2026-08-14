package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO;
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
class MoodEntryControllerTest {

    @Mock
    private MoodEntryService moodEntryService;

    @InjectMocks
    private MoodEntryController moodEntryController;

    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T00:00:00Z");

    // Test data representing different mood entry scenarios
    private MoodEntryRequestDTO testRequestDTO;
    private MoodEntryResponseDTO savedResponseDTO;
    private MoodEntryResponseDTO updatedResponseDTO;

    // Test userId for consistent authentication context (not exposed in DTOs)
    private static final String TEST_USER_ID = "user_test123";

    @BeforeEach
    void setUp() {
        testRequestDTO = new MoodEntryRequestDTO();
        testRequestDTO.setMoodScore((short) 4);
        testRequestDTO.setDateTime(FIXED_NOW);
        testRequestDTO.setFactors(List.of("Work", "Stress"));
        testRequestDTO.setNotes("Had a tough day.");

        // Expected service response after successful creation
        savedResponseDTO = new MoodEntryResponseDTO();
        savedResponseDTO.setId(1L);
        savedResponseDTO.setMoodScore((short) 4);
        savedResponseDTO.setDateTime(FIXED_NOW);
        savedResponseDTO.setFactors(List.of("Work", "Stress"));
        savedResponseDTO.setNotes("Had a tough day.");

        // Improved mood state for update testing
        updatedResponseDTO = new MoodEntryResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setMoodScore((short) 5);
        updatedResponseDTO.setDateTime(FIXED_NOW.plusSeconds(10));
        updatedResponseDTO.setFactors(List.of("Achievement", "Relaxation"));
        updatedResponseDTO.setNotes("Feeling fantastic after finishing the task!");
    }

    @Test
    void createMoodEntry_ShouldReturnCreatedMoodEntry() {
        when(moodEntryService.createMoodEntry(any(MoodEntryRequestDTO.class))).thenReturn(savedResponseDTO);

        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.createMoodEntry(testRequestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(moodEntryService).createMoodEntry(testRequestDTO);
    }

    @Test
    void getAllMoodEntries_ShouldReturnAllMoodEntries() {
        MoodEntryResponseDTO secondEntry = new MoodEntryResponseDTO();
        secondEntry.setId(2L);
        secondEntry.setMoodScore((short) 3);
        secondEntry.setDateTime(FIXED_NOW.plusSeconds(7200));
        List<MoodEntryResponseDTO> expectedEntries = List.of(savedResponseDTO, secondEntry);
        when(moodEntryService.getAllMoodEntries()).thenReturn(expectedEntries);

        ResponseEntity<List<MoodEntryResponseDTO>> response = moodEntryController.getAllMoodEntries(null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2)
                .first()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(moodEntryService).getAllMoodEntries();
    }

    @Test
    void getAllMoodEntries_ShouldReturnNoContentWhenEmpty() {
        when(moodEntryService.getAllMoodEntries()).thenReturn(List.of());

        ResponseEntity<List<MoodEntryResponseDTO>> response = moodEntryController.getAllMoodEntries(null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(moodEntryService).getAllMoodEntries();
    }

    @Test
    void getMoodEntryById_ShouldReturnMoodEntryWhenFound() {
        when(moodEntryService.getMoodEntryById(anyLong())).thenReturn(savedResponseDTO);

        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.getMoodEntryById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(moodEntryService).getMoodEntryById(1L);
    }

    @Test
    void getMoodEntryById_ShouldReturnNotFoundWhenNotFound() {
        when(moodEntryService.getMoodEntryById(anyLong())).thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.getMoodEntryById(99L));
        verify(moodEntryService).getMoodEntryById(99L);
    }

    @Test
    void updateMoodEntry_ShouldReturnUpdatedMoodEntryWhenFound() {
        when(moodEntryService.updateMoodEntry(anyLong(), any(MoodEntryRequestDTO.class))).thenReturn(updatedResponseDTO);

        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.updateMoodEntry(1L, testRequestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedResponseDTO);
        verify(moodEntryService).updateMoodEntry(1L, testRequestDTO);
    }

    @Test
    void updateMoodEntry_ShouldReturnNotFoundWhenNotFound() {
        when(moodEntryService.updateMoodEntry(anyLong(), any(MoodEntryRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Not found for update"));

        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.updateMoodEntry(99L, testRequestDTO));
        verify(moodEntryService).updateMoodEntry(99L, testRequestDTO);
    }

    @Test
    void deleteMoodEntry_ShouldReturnNoContentWhenFound() {
        doNothing().when(moodEntryService).deleteMoodEntry(anyLong());

        ResponseEntity<Void> response = moodEntryController.deleteMoodEntry(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(moodEntryService).deleteMoodEntry(1L);
    }

    @Test
    void deleteMoodEntry_ShouldReturnNotFoundWhenNotFound() {
        doThrow(new ResourceNotFoundException("Not found for delete")).when(moodEntryService).deleteMoodEntry(anyLong());

        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.deleteMoodEntry(99L));
        verify(moodEntryService).deleteMoodEntry(99L);
    }
}