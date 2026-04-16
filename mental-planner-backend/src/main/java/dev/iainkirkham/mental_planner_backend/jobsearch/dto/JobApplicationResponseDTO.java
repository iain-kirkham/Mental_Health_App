package dev.iainkirkham.mental_planner_backend.jobsearch.dto;

import dev.iainkirkham.mental_planner_backend.jobsearch.JobApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponseDTO {

    private Long id;
    private String companyName;
    private String roleTitle;
    private JobApplicationStatus status;
}

