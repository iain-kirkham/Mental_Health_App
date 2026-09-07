package dev.iainkirkham.mental_planner_backend.tasks;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Task's tracked-time invariant: applyManualEntry/unwindManualEntry accumulate
 * manual entries onto actualMinutes, while recordTimerCheckpoint sets it directly for
 * stopwatch/focus runs which are recorded as history separately.
 */
class TaskTest {

    @Test
    void applyManualEntry_addsOntoActualMinutes() {
        Task task = new Task();
        task.applyManualEntry(15);
        task.applyManualEntry(10);

        assertThat(task.getActualMinutes()).isEqualTo(25);
    }

    @Test
    void unwindManualEntry_subtractsFromActualMinutes() {
        Task task = new Task();
        task.applyManualEntry(30);

        task.unwindManualEntry(10);

        assertThat(task.getActualMinutes()).isEqualTo(20);
    }

    @Test
    void unwindManualEntry_clampsAtZero() {
        Task task = new Task();
        task.applyManualEntry(5);

        task.unwindManualEntry(20);

        assertThat(task.getActualMinutes()).isZero();
    }

    @Test
    void recordTimerCheckpoint_setsActualMinutesDirectly() {
        Task task = new Task();
        task.applyManualEntry(15);

        task.recordTimerCheckpoint(42);

        assertThat(task.getActualMinutes()).isEqualTo(42);
    }

    @Test
    void recordTimerCheckpoint_doesNotAccumulate() {
        Task task = new Task();
        task.recordTimerCheckpoint(10);
        task.recordTimerCheckpoint(12);

        assertThat(task.getActualMinutes()).isEqualTo(12);
    }

    @Test
    void setPriority_StoresGivenPriority() {
        Task task = new Task();
        task.setPriority(TaskPriority.URGENT);

        assertThat(task.getPriority()).isEqualTo(TaskPriority.URGENT);
    }

    @Test
    void setPriority_WithNull_DefaultsToNormal() {
        Task task = new Task();
        task.setPriority(TaskPriority.URGENT);

        task.setPriority(null);

        assertThat(task.getPriority()).isEqualTo(TaskPriority.NORMAL);
    }
}
