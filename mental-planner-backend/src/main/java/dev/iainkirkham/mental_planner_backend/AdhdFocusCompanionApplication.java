package dev.iainkirkham.mental_planner_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for ADHD Focus Companion Backend
 * Provides REST APIs for Pomodoro session tracking and mood logging
 */
@SpringBootApplication
public class AdhdFocusCompanionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdhdFocusCompanionApplication.class, args);
	}

}
