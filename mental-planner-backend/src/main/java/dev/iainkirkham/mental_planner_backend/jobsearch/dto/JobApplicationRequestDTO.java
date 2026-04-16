package dev.iainkirkham.mental_planner_backend.jobsearch.dto;

import dev.iainkirkham.mental_planner_backend.jobsearch.JobApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationRequestDTO {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Role title is required")
    private String roleTitle;

    @NotNull(message = "Status is required")
    private JobApplicationStatus status;
}

