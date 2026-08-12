package dev.iainkirkham.mental_planner_backend.jobsearch;

import dev.iainkirkham.mental_planner_backend.jobsearch.dto.JobApplicationRequestDTO;
import dev.iainkirkham.mental_planner_backend.jobsearch.dto.JobApplicationResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerTest {

    @Mock
    private JobApplicationService jobApplicationService;

    @InjectMocks
    private JobApplicationController jobApplicationController;

    @Test
    void createJobApplication_ShouldReturnCreated() {
        JobApplicationRequestDTO request = new JobApplicationRequestDTO("Acme", "Backend Engineer", JobApplicationStatus.APPLIED);
        JobApplicationResponseDTO responseDto = new JobApplicationResponseDTO(1L, "Acme", "Backend Engineer", JobApplicationStatus.APPLIED);
        when(jobApplicationService.createJobApplication(any(JobApplicationRequestDTO.class))).thenReturn(responseDto);

        ResponseEntity<JobApplicationResponseDTO> response = jobApplicationController.createJobApplication(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(responseDto);
        verify(jobApplicationService).createJobApplication(request);
    }

    @Test
    void getAllJobApplications_ShouldReturnNoContentWhenEmpty() {
        when(jobApplicationService.getAllJobApplications()).thenReturn(List.of());

        ResponseEntity<List<JobApplicationResponseDTO>> response = jobApplicationController.getAllJobApplications();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(jobApplicationService).getAllJobApplications();
    }

    @Test
    void deleteJobApplication_ShouldReturnNoContent() {
        doNothing().when(jobApplicationService).deleteJobApplication(anyLong());

        ResponseEntity<Void> response = jobApplicationController.deleteJobApplication(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(jobApplicationService).deleteJobApplication(1L);
    }
}

