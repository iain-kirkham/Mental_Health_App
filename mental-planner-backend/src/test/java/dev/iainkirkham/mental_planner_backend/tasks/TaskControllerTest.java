package dev.iainkirkham.mental_planner_backend.tasks;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskResponseDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.ActualMinutesRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.CompletionRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskReorderItemDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.tasks.dto.TaskResponseDTO;
import org.junit.jupiter.api.BeforeEach;
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
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private static final LocalDate FIXED_DATE = LocalDate.parse("2025-12-01");

    private TaskRequestDTO testRequestDTO;
    private TaskResponseDTO savedResponseDTO;
    private TaskResponseDTO updatedResponseDTO;

    @BeforeEach
    void setUp() {
        testRequestDTO = new TaskRequestDTO();
        testRequestDTO.setTitle("Write report");
        testRequestDTO.setScheduledDate(FIXED_DATE);
        testRequestDTO.setPriority(TaskPriority.NORMAL);

        savedResponseDTO = new TaskResponseDTO();
        savedResponseDTO.setId(1L);
        savedResponseDTO.setTitle("Write report");
        savedResponseDTO.setScheduledDate(FIXED_DATE);
        savedResponseDTO.setPriority(TaskPriority.NORMAL);
        savedResponseDTO.setSubtasks(List.of());

        updatedResponseDTO = new TaskResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setTitle("Write final report");
        updatedResponseDTO.setScheduledDate(FIXED_DATE.plusDays(1));
        updatedResponseDTO.setPriority(TaskPriority.HIGH);
        updatedResponseDTO.setSubtasks(List.of());
    }

    @Test
    void createTask_ShouldReturnCreatedTask() {
        when(taskService.createTask(any(TaskRequestDTO.class))).thenReturn(savedResponseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.createTask(testRequestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(taskService).createTask(testRequestDTO);
    }

    @Test
    void getTasks_ShouldReturnTasksForExplicitDate() {
        when(taskService.getTasksForDate(FIXED_DATE)).thenReturn(List.of(savedResponseDTO));

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getTasks(FIXED_DATE, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(taskService).getTasksForDate(FIXED_DATE);
    }

    @Test
    void getTasks_ShouldReturnTasksForDateRangeWhenBothDatesGiven() {
        when(taskService.getTasksForDateRange(FIXED_DATE, FIXED_DATE.plusDays(6)))
                .thenReturn(List.of(savedResponseDTO));

        ResponseEntity<List<TaskResponseDTO>> response =
                taskController.getTasks(null, FIXED_DATE, FIXED_DATE.plusDays(6));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(taskService).getTasksForDateRange(FIXED_DATE, FIXED_DATE.plusDays(6));
    }

    @Test
    void getTasks_ShouldReturnNoContentWhenEmpty() {
        when(taskService.getTasksForDate(any(LocalDate.class))).thenReturn(List.of());

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getTasks(FIXED_DATE, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getTaskById_ShouldReturnTaskWhenFound() {
        when(taskService.getTaskById(anyLong())).thenReturn(savedResponseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.getTaskById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(taskService).getTaskById(1L);
    }

    @Test
    void getTaskById_ShouldReturnNotFoundWhenNotFound() {
        when(taskService.getTaskById(anyLong())).thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> taskController.getTaskById(99L));
        verify(taskService).getTaskById(99L);
    }

    @Test
    void updateTask_ShouldReturnUpdatedTaskWhenFound() {
        when(taskService.updateTask(anyLong(), any(TaskRequestDTO.class))).thenReturn(updatedResponseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.updateTask(1L, testRequestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedResponseDTO);
        verify(taskService).updateTask(1L, testRequestDTO);
    }

    @Test
    void updateTask_ShouldReturnNotFoundWhenNotFound() {
        when(taskService.updateTask(anyLong(), any(TaskRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Not found for update"));

        assertThrows(ResourceNotFoundException.class, () -> taskController.updateTask(99L, testRequestDTO));
        verify(taskService).updateTask(99L, testRequestDTO);
    }

    @Test
    void setCompletionCascade_ShouldReturnUpdatedTaskWithCascadedSubtasks() {
        CompletionRequestDTO requestDTO = new CompletionRequestDTO();
        requestDTO.setCompleted(true);

        TaskResponseDTO cascaded = new TaskResponseDTO();
        cascaded.setId(1L);
        cascaded.setCompleted(true);
        SubtaskResponseDTO subtask = new SubtaskResponseDTO();
        subtask.setId(10L);
        subtask.setTaskId(1L);
        subtask.setCompleted(true);
        cascaded.setSubtasks(List.of(subtask));

        when(taskService.setCompletionCascade(1L, requestDTO)).thenReturn(cascaded);

        ResponseEntity<TaskResponseDTO> response = taskController.setCompletionCascade(1L, requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSubtasks()).hasSize(1);
        assertThat(response.getBody().getSubtasks().get(0).isCompleted()).isTrue();
        verify(taskService).setCompletionCascade(1L, requestDTO);
    }

    @Test
    void setCompletionCascade_ShouldReturnNotFoundWhenNotFound() {
        CompletionRequestDTO requestDTO = new CompletionRequestDTO();
        requestDTO.setCompleted(true);
        when(taskService.setCompletionCascade(anyLong(), any(CompletionRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> taskController.setCompletionCascade(99L, requestDTO));
    }

    @Test
    void updateActualMinutes_ShouldReturnUpdatedTask() {
        ActualMinutesRequestDTO requestDTO = new ActualMinutesRequestDTO();
        requestDTO.setActualMinutes(42);

        TaskResponseDTO updated = new TaskResponseDTO();
        updated.setId(1L);
        updated.setActualMinutes(42);
        when(taskService.updateActualMinutes(1L, requestDTO)).thenReturn(updated);

        ResponseEntity<TaskResponseDTO> response = taskController.updateActualMinutes(1L, requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getActualMinutes()).isEqualTo(42);
        verify(taskService).updateActualMinutes(1L, requestDTO);
    }

    @Test
    void reorderTasks_ShouldReturnReorderedTasks() {
        TaskReorderItemDTO item1 = new TaskReorderItemDTO(1L, 0);
        TaskReorderItemDTO item2 = new TaskReorderItemDTO(2L, 1);
        List<TaskReorderItemDTO> items = List.of(item1, item2);

        TaskResponseDTO reordered1 = new TaskResponseDTO();
        reordered1.setId(1L);
        reordered1.setSortOrder(0);
        TaskResponseDTO reordered2 = new TaskResponseDTO();
        reordered2.setId(2L);
        reordered2.setSortOrder(1);

        when(taskService.reorderTasks(items)).thenReturn(List.of(reordered1, reordered2));

        ResponseEntity<List<TaskResponseDTO>> response = taskController.reorderTasks(items);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        verify(taskService).reorderTasks(items);
    }

    @Test
    void reorderTasks_ShouldReturnNotFoundWhenAnyTaskNotOwned() {
        List<TaskReorderItemDTO> items = List.of(new TaskReorderItemDTO(99L, 0));
        when(taskService.reorderTasks(items)).thenThrow(new ResourceNotFoundException("Task not found with ID: 99"));

        assertThrows(ResourceNotFoundException.class, () -> taskController.reorderTasks(items));
    }

    @Test
    void deleteTask_ShouldReturnNoContentWhenFound() {
        doNothing().when(taskService).deleteTask(anyLong());

        ResponseEntity<Void> response = taskController.deleteTask(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(taskService).deleteTask(1L);
    }

    @Test
    void deleteTask_ShouldReturnNotFoundWhenNotFound() {
        doThrow(new ResourceNotFoundException("Not found for delete")).when(taskService).deleteTask(anyLong());

        assertThrows(ResourceNotFoundException.class, () -> taskController.deleteTask(99L));
    }

    @Test
    void archiveTask_ShouldReturnArchivedTask() {
        TaskResponseDTO archived = new TaskResponseDTO();
        archived.setId(1L);
        archived.setArchived(true);
        when(taskService.archiveTask(1L)).thenReturn(archived);

        ResponseEntity<TaskResponseDTO> response = taskController.archiveTask(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isArchived()).isTrue();
        verify(taskService).archiveTask(1L);
    }
}
