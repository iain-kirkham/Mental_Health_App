package dev.iainkirkham.mental_planner_backend.jobsearch;

import dev.iainkirkham.mental_planner_backend.jobsearch.dto.JobApplicationRequestDTO;
import dev.iainkirkham.mental_planner_backend.jobsearch.dto.JobApplicationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/job-search")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponseDTO> createJobApplication(@RequestBody @Valid JobApplicationRequestDTO requestDTO) {
        JobApplicationResponseDTO saved = jobApplicationService.createJobApplication(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<JobApplicationResponseDTO>> getAllJobApplications() {
        List<JobApplicationResponseDTO> applications = jobApplicationService.getAllJobApplications();
        if (applications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponseDTO> getJobApplicationById(@PathVariable Long id) {
        JobApplicationResponseDTO application = jobApplicationService.getJobApplicationById(id);
        return ResponseEntity.ok(application);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponseDTO> updateJobApplication(@PathVariable Long id,
                                                                          @RequestBody @Valid JobApplicationRequestDTO requestDTO) {
        JobApplicationResponseDTO updated = jobApplicationService.updateJobApplication(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable Long id) {
        jobApplicationService.deleteJobApplication(id);
        return ResponseEntity.noContent().build();
    }
}

