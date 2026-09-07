import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { useTimerStore } from "./timerStore";

function resetStore() {
  useTimerStore.setState({
    mode: "stopwatch",
    activeTaskId: null,
    sessionLengthMinutes: null,
    startedAt: null,
    elapsedWhenPaused: 0,
    elapsedSeconds: 0,
    isRunning: false,
    baseActualMinutes: 0,
    firstStartedAt: null,
    runKey: null,
    runStartActualMinutes: null,
    intervalId: null,
    lastCompletedSession: null,
  });
}

describe("timerStore", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    resetStore();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("logs a FOCUS entry when a focus session completes naturally", () => {
    const logEntryFn = vi.fn();
    useTimerStore.getState().setPersistFn(vi.fn());
    useTimerStore.getState().setLogEntryFn(logEntryFn);

    useTimerStore.getState().startTimer(1, 0, { mode: "focus", sessionLengthMinutes: 1 });
    vi.advanceTimersByTime(60_000);

    expect(logEntryFn).toHaveBeenCalledTimes(1);
    expect(logEntryFn).toHaveBeenCalledWith(
      expect.objectContaining({ taskId: 1, source: "FOCUS", minutes: 1 })
    );
  });

  it("does not log an entry when a focus session is cancelled before completion", () => {
    const logEntryFn = vi.fn();
    useTimerStore.getState().setPersistFn(vi.fn());
    useTimerStore.getState().setLogEntryFn(logEntryFn);

    useTimerStore.getState().startTimer(1, 0, { mode: "focus", sessionLengthMinutes: 25 });
    useTimerStore.getState().cancelTimer();

    expect(logEntryFn).not.toHaveBeenCalled();
  });

  it("still logs a STOPWATCH entry on stopTimer (regression guard)", () => {
    const logEntryFn = vi.fn();
    useTimerStore.getState().setPersistFn(vi.fn());
    useTimerStore.getState().setLogEntryFn(logEntryFn);

    useTimerStore.getState().startTimer(1, 0);
    vi.advanceTimersByTime(60_000);
    useTimerStore.getState().stopTimer();

    expect(logEntryFn).toHaveBeenCalledWith(expect.objectContaining({ taskId: 1, source: "STOPWATCH" }));
  });

  it("logs a task-less FOCUS entry when no task is linked", () => {
    const logEntryFn = vi.fn();
    const persistFn = vi.fn();
    useTimerStore.getState().setPersistFn(persistFn);
    useTimerStore.getState().setLogEntryFn(logEntryFn);

    useTimerStore.getState().startTimer(null, 0, { mode: "focus", sessionLengthMinutes: 1 });
    vi.advanceTimersByTime(60_000);

    expect(logEntryFn).toHaveBeenCalledTimes(1);
    expect(logEntryFn).toHaveBeenCalledWith(
      expect.objectContaining({ taskId: null, source: "FOCUS", minutes: 1 })
    );
    // No task means nothing to update actualMinutes on.
    expect(persistFn).not.toHaveBeenCalled();
  });

  it("does not double-count elapsed time across a pause/resume cycle", () => {
    const logEntryFn = vi.fn();
    const persistFn = vi.fn();
    useTimerStore.getState().setPersistFn(persistFn);
    useTimerStore.getState().setLogEntryFn(logEntryFn);

    // Start a 25-minute focus session against a task with 0 prior actualMinutes.
    useTimerStore.getState().startTimer(1, 0, { mode: "focus", sessionLengthMinutes: 25 });
    vi.advanceTimersByTime(5 * 60_000); // 5 minutes in
    useTimerStore.getState().pauseTimer();
    // Real call sites always pass the mode explicitly (see Timer.tsx/TaskDetailModal), which is
    // what the resume-detection branch (activeTaskId + mode match) relies on.
    useTimerStore.getState().startTimer(1, 0, { mode: "focus", sessionLengthMinutes: 25 }); // resume
    vi.advanceTimersByTime(20 * 60_000); // remaining 20 minutes

    expect(logEntryFn).toHaveBeenCalledTimes(1);
    expect(logEntryFn).toHaveBeenCalledWith(expect.objectContaining({ minutes: 25 }));
    expect(persistFn).toHaveBeenLastCalledWith(1, 25);
  });
});
