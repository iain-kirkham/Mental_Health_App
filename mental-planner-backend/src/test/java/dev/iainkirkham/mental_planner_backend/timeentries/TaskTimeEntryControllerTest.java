package dev.iainkirkham.mental_planner_backend.timeentries;

import dev.iainkirkham.mental_planner_backend.timeentries.dto.TaskTimeEntryResponseDTO;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskTimeEntryControllerTest {

    @Mock
    private TimeEntryService timeEntryService;

    @InjectMocks
    private TaskTimeEntryController taskTimeEntryController;

    private static final LocalDate FIXED_DATE = LocalDate.parse("2025-12-01");

    @Test
    void getTimeEntriesForTask_ShouldReturnEntriesWhenPresent() {
        TaskTimeEntryResponseDTO entry = new TaskTimeEntryResponseDTO();
        entry.setId(1L);
        entry.setTaskId(1L);
        entry.setMinutes(30);
        entry.setEntryDate(FIXED_DATE);
        entry.setSource(TimeEntrySource.STOPWATCH);
        when(timeEntryService.getTimeEntriesForTask(1L)).thenReturn(List.of(entry));

        ResponseEntity<List<TaskTimeEntryResponseDTO>> response = taskTimeEntryController.getTimeEntriesForTask(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(timeEntryService).getTimeEntriesForTask(1L);
    }

    @Test
    void getTimeEntriesForTask_ShouldReturnNoContentWhenEmpty() {
        when(timeEntryService.getTimeEntriesForTask(1L)).thenReturn(List.of());

        ResponseEntity<List<TaskTimeEntryResponseDTO>> response = taskTimeEntryController.getTimeEntriesForTask(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
