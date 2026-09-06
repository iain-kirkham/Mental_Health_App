package dev.iainkirkham.mental_planner_backend.subtasks;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskRequestDTO;
import dev.iainkirkham.mental_planner_backend.subtasks.dto.SubtaskResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubtaskControllerTest {

    @Mock
    private SubtaskService subtaskService;

    @InjectMocks
    private SubtaskController subtaskController;

    @Test
    void createSubtask_ShouldReturnCreatedSubtask() {
        SubtaskRequestDTO requestDTO = new SubtaskRequestDTO();
        requestDTO.setTitle("Draft outline");

        SubtaskResponseDTO created = new SubtaskResponseDTO();
        created.setId(10L);
        created.setTaskId(1L);
        created.setTitle("Draft outline");
        when(subtaskService.createSubtask(1L, requestDTO)).thenReturn(created);

        ResponseEntity<SubtaskResponseDTO> response = subtaskController.createSubtask(1L, requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(created);
        verify(subtaskService).createSubtask(1L, requestDTO);
    }

    @Test
    void createSubtask_ShouldReturnNotFoundWhenParentTaskNotOwned() {
        SubtaskRequestDTO requestDTO = new SubtaskRequestDTO();
        requestDTO.setTitle("Draft outline");
        when(subtaskService.createSubtask(anyLong(), any(SubtaskRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Task not found with ID: 99"));

        assertThrows(ResourceNotFoundException.class, () -> subtaskController.createSubtask(99L, requestDTO));
    }

    @Test
    void updateSubtask_ShouldReturnUpdatedSubtask() {
        SubtaskRequestDTO requestDTO = new SubtaskRequestDTO();
        requestDTO.setTitle("Renamed");
        requestDTO.setCompleted(true);

        SubtaskResponseDTO updated = new SubtaskResponseDTO();
        updated.setId(10L);
        updated.setTaskId(1L);
        updated.setTitle("Renamed");
        updated.setCompleted(true);
        when(subtaskService.updateSubtask(1L, 10L, requestDTO)).thenReturn(updated);

        ResponseEntity<SubtaskResponseDTO> response = subtaskController.updateSubtask(1L, 10L, requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updated);
        verify(subtaskService).updateSubtask(1L, 10L, requestDTO);
    }

    @Test
    void deleteSubtask_ShouldReturnNoContentWhenFound() {
        doNothing().when(subtaskService).deleteSubtask(1L, 10L);

        ResponseEntity<Void> response = subtaskController.deleteSubtask(1L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(subtaskService).deleteSubtask(1L, 10L);
    }

    @Test
    void deleteSubtask_ShouldReturnNotFoundWhenNotFound() {
        doThrow(new ResourceNotFoundException("Subtask not found"))
                .when(subtaskService).deleteSubtask(1L, 99L);

        assertThrows(ResourceNotFoundException.class, () -> subtaskController.deleteSubtask(1L, 99L));
    }
}
