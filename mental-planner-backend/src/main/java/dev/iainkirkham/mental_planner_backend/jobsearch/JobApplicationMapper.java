package dev.iainkirkham.mental_planner_backend.jobsearch;

import dev.iainkirkham.mental_planner_backend.jobsearch.dto.JobApplicationRequestDTO;
import dev.iainkirkham.mental_planner_backend.jobsearch.dto.JobApplicationResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobApplicationMapper {

    public JobApplication toEntity(JobApplicationRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        JobApplication entity = new JobApplication();
        entity.setCompanyName(dto.getCompanyName());
        entity.setRoleTitle(dto.getRoleTitle());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    public JobApplicationResponseDTO toResponseDTO(JobApplication entity) {
        if (entity == null) {
            return null;
        }

        JobApplicationResponseDTO dto = new JobApplicationResponseDTO();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setRoleTitle(entity.getRoleTitle());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public List<JobApplicationResponseDTO> toResponseDTOList(List<JobApplication> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(JobApplication entity, JobApplicationRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setCompanyName(dto.getCompanyName());
        entity.setRoleTitle(dto.getRoleTitle());
        entity.setStatus(dto.getStatus());
    }
}

