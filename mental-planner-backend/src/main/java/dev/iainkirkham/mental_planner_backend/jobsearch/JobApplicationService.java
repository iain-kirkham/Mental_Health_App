package dev.iainkirkham.mental_planner_backend.jobsearch;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.jobsearch.dto.JobApplicationRequestDTO;
import dev.iainkirkham.mental_planner_backend.jobsearch.dto.JobApplicationResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final AuthenticationContext authenticationContext;
    private final JobApplicationMapper jobApplicationMapper;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                                 AuthenticationContext authenticationContext,
                                 JobApplicationMapper jobApplicationMapper) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.authenticationContext = authenticationContext;
        this.jobApplicationMapper = jobApplicationMapper;
    }

    public JobApplicationResponseDTO createJobApplication(JobApplicationRequestDTO requestDTO) {
        JobApplication jobApplication = jobApplicationMapper.toEntity(requestDTO);
        jobApplication.setId(null);
        jobApplication.setUserId(authenticationContext.getCurrentUserId());
        JobApplication saved = jobApplicationRepository.save(jobApplication);
        return jobApplicationMapper.toResponseDTO(saved);
    }

    public List<JobApplicationResponseDTO> getAllJobApplications() {
        String userId = authenticationContext.getCurrentUserId();
        List<JobApplication> applications = jobApplicationRepository.findByUserIdOrderByIdDesc(userId);
        return jobApplicationMapper.toResponseDTOList(applications);
    }

    public JobApplicationResponseDTO getJobApplicationById(Long id) {
        String userId = authenticationContext.getCurrentUserId();
        JobApplication application = jobApplicationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Job application not found with ID: " + id));
        return jobApplicationMapper.toResponseDTO(application);
    }

    public JobApplicationResponseDTO updateJobApplication(Long id, JobApplicationRequestDTO requestDTO) {
        String userId = authenticationContext.getCurrentUserId();
        JobApplication existing = jobApplicationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Job application not found with ID: " + id));

        jobApplicationMapper.updateEntityFromDTO(existing, requestDTO);

        JobApplication updated = jobApplicationRepository.save(existing);
        return jobApplicationMapper.toResponseDTO(updated);
    }

    public void deleteJobApplication(Long id) {
        String userId = authenticationContext.getCurrentUserId();
        JobApplication application = jobApplicationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Job application not found with ID: " + id));
        jobApplicationRepository.delete(application);
    }
}

