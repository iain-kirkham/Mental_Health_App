package dev.iainkirkham.mental_planner_backend.taskplanner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    // Custom query method to find subtasks by their parent task's ID
    List<SubTask> findByTaskId(Long taskId);
}
