package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
import dev.iainkirkham.mental_planner_backend.timeentries.dto.TimeEntryReflectionRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
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
class TimeEntryControllerTest {

    @Mock
    private TimeEntryService timeEntryService;

    @InjectMocks
    private TimeEntryController timeEntryController;

    private static final LocalDate FIXED_DATE = LocalDate.parse("2025-12-01");

    @Test
    void getTimeEntries_ShouldReturnEntriesWhenPresent() {
        TaskTimeEntryResponseDTO entry = new TaskTimeEntryResponseDTO();
        entry.setId(1L);
        entry.setTaskId(1L);
        entry.setMinutes(30);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.STOPWATCH);
        when(timeEntryService.getTimeEntries(FIXED_DATE, FIXED_DATE)).thenReturn(List.of(entry));

        ResponseEntity<List<TaskTimeEntryResponseDTO>> response =
                timeEntryController.getTimeEntries(FIXED_DATE, FIXED_DATE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(timeEntryService).getTimeEntries(FIXED_DATE, FIXED_DATE);
    }

    @Test
    void getTimeEntries_ShouldReturnNoContentWhenEmpty() {
        when(timeEntryService.getTimeEntries(FIXED_DATE, FIXED_DATE)).thenReturn(List.of());

        ResponseEntity<List<TaskTimeEntryResponseDTO>> response =
                timeEntryController.getTimeEntries(FIXED_DATE, FIXED_DATE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void logTimeEntry_ShouldReturnCreatedEntry() {
        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setTaskId(1L);
        requestDTO.setMinutes(20);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.MANUAL);

        TaskTimeEntryResponseDTO created = new TaskTimeEntryResponseDTO();
        created.setId(1L);
        created.setTaskId(1L);
        created.setMinutes(20);
        created.setEntryDate(FIXED_DATE);
        created.setSource(TimeEntrySource.MANUAL);
        when(timeEntryService.logTimeEntry(requestDTO)).thenReturn(created);

        ResponseEntity<TaskTimeEntryResponseDTO> response = timeEntryController.logTimeEntry(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(created);
        verify(timeEntryService).logTimeEntry(requestDTO);
    }

    @Test
    void logTimeEntry_ShouldReturnNotFoundWhenTaskNotOwned() {
        TaskTimeEntryRequestDTO requestDTO = new TaskTimeEntryRequestDTO();
        requestDTO.setTaskId(99L);
        requestDTO.setMinutes(20);
        requestDTO.setEntryDate(FIXED_DATE);
        requestDTO.setSource(TimeEntrySource.MANUAL);
        when(timeEntryService.logTimeEntry(any(TaskTimeEntryRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Task not found with ID: 99"));

        assertThrows(ResourceNotFoundException.class, () -> timeEntryController.logTimeEntry(requestDTO));
    }

    @Test
    void updateTimeEntryReflection_ShouldReturnUpdatedEntry() {
        TimeEntryReflectionRequestDTO requestDTO = new TimeEntryReflectionRequestDTO();
        requestDTO.setScore((short) 4);
        requestDTO.setNotes("Went well");

        TaskTimeEntryResponseDTO updated = new TaskTimeEntryResponseDTO();
        updated.setId(1L);
        updated.setScore((short) 4);
        updated.setNotes("Went well");
        when(timeEntryService.updateTimeEntryReflection(1L, requestDTO)).thenReturn(updated);

        ResponseEntity<TaskTimeEntryResponseDTO> response =
                timeEntryController.updateTimeEntryReflection(1L, requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updated);
        verify(timeEntryService).updateTimeEntryReflection(1L, requestDTO);
    }

    @Test
    void updateTimeEntryReflection_ShouldThrowWhenNotFound() {
        TimeEntryReflectionRequestDTO requestDTO = new TimeEntryReflectionRequestDTO();
        when(timeEntryService.updateTimeEntryReflection(anyLong(), any(TimeEntryReflectionRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Time entry not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> timeEntryController.updateTimeEntryReflection(99L, requestDTO));
    }

    @Test
    void deleteTimeEntry_ShouldReturnNoContentWhenFound() {
        doNothing().when(timeEntryService).deleteTimeEntry(10L);

        ResponseEntity<Void> response = timeEntryController.deleteTimeEntry(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(timeEntryService).deleteTimeEntry(10L);
    }

    @Test
    void deleteTimeEntry_ShouldReturnNotFoundWhenNotFound() {
        doThrow(new ResourceNotFoundException("Time entry not found"))
                .when(timeEntryService).deleteTimeEntry(99L);

        assertThrows(ResourceNotFoundException.class, () -> timeEntryController.deleteTimeEntry(99L));
    }
}
