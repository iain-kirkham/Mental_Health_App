package dev.iainkirkham.mental_planner_backend.jobsearch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUserIdOrderByIdDesc(String userId);

    Optional<JobApplication> findByIdAndUserId(Long id, String userId);
}

