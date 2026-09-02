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

  it("logs a COUNTDOWN entry when a countdown session completes naturally", () => {
    const logEntryFn = vi.fn();
    useTimerStore.getState().setPersistFn(vi.fn());
    useTimerStore.getState().setLogEntryFn(logEntryFn);

    useTimerStore.getState().startTimer(1, 0, { mode: "countdown", sessionLengthMinutes: 1 });
    vi.advanceTimersByTime(60_000);

    expect(logEntryFn).toHaveBeenCalledTimes(1);
    expect(logEntryFn).toHaveBeenCalledWith(1, expect.objectContaining({ source: "COUNTDOWN", minutes: 1 }));
  });

  it("does not log an entry when a countdown session is cancelled before completion", () => {
    const logEntryFn = vi.fn();
    useTimerStore.getState().setPersistFn(vi.fn());
    useTimerStore.getState().setLogEntryFn(logEntryFn);

    useTimerStore.getState().startTimer(1, 0, { mode: "countdown", sessionLengthMinutes: 25 });
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

    expect(logEntryFn).toHaveBeenCalledWith(1, expect.objectContaining({ source: "STOPWATCH" }));
  });
});
