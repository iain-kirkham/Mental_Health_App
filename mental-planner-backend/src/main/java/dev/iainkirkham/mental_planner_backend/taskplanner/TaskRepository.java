package dev.iainkirkham.mental_planner_backend.taskplanner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // Custom query method to find tasks by date
    List<Task> findByDate(LocalDate date);

    // Custom query method to find tasks within a date range
    List<Task> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
