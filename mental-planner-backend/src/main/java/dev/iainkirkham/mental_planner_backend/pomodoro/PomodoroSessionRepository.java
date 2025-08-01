package dev.iainkirkham.mental_planner_backend.pomodoro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {

}
