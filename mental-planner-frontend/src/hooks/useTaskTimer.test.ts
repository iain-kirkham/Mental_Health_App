import { describe, expect, it } from "vitest";
import { computeCountdownView, computeLiveActualView, type TimerSnapshot } from "./useTaskTimer";
import type { TaskResponseDTO } from "@/types";

function task(overrides: Partial<TaskResponseDTO> & { id: number }): TaskResponseDTO {
  return {
    title: `Task ${overrides.id}`,
    description: null,
    scheduledDate: "2026-08-18",
    startTime: null,
    endTime: null,
    completed: false,
    sortOrder: 0,
    plannedMinutes: null,
    actualMinutes: 0,
    category: null,
    archived: false,
    priority: "NORMAL",
    subtasks: [],
    ...overrides,
  };
}

function snapshot(overrides: Partial<TimerSnapshot> = {}): TimerSnapshot {
  return {
    activeTaskId: null,
    isRunning: false,
    mode: "stopwatch",
    sessionLengthMinutes: null,
    elapsedSeconds: 0,
    ...overrides,
  };
}

describe("computeCountdownView", () => {
  it("is inactive when no task is given", () => {
    const view = computeCountdownView(snapshot({ activeTaskId: 1, isRunning: true, mode: "countdown" }), null);
    expect(view.isActiveHere).toBe(false);
    expect(view.running).toBe(false);
    expect(view.secondsLeft).toBeNull();
  });

  it("is inactive when the active task is a different one", () => {
    const t = task({ id: 1 });
    const view = computeCountdownView(snapshot({ activeTaskId: 2, isRunning: true, mode: "countdown" }), t);
    expect(view.isActiveHere).toBe(false);
  });

  it("is inactive when the store is in stopwatch mode", () => {
    const t = task({ id: 1 });
    const view = computeCountdownView(snapshot({ activeTaskId: 1, isRunning: true, mode: "stopwatch" }), t);
    expect(view.isActiveHere).toBe(false);
    expect(view.secondsLeft).toBeNull();
  });

  it("reports active + running + remaining seconds during a countdown for this task", () => {
    const t = task({ id: 1 });
    const view = computeCountdownView(
      snapshot({ activeTaskId: 1, isRunning: true, mode: "countdown", sessionLengthMinutes: 25, elapsedSeconds: 60 }),
      t
    );
    expect(view.isActiveHere).toBe(true);
    expect(view.running).toBe(true);
    expect(view.secondsLeft).toBe(25 * 60 - 60);
  });

  it("reports active but not running when this task's session is paused", () => {
    const t = task({ id: 1 });
    const view = computeCountdownView(
      snapshot({ activeTaskId: 1, isRunning: false, mode: "countdown", sessionLengthMinutes: 25, elapsedSeconds: 60 }),
      t
    );
    expect(view.isActiveHere).toBe(true);
    expect(view.running).toBe(false);
  });

  it("floors secondsLeft at 0 rather than going negative", () => {
    const t = task({ id: 1 });
    const view = computeCountdownView(
      snapshot({ activeTaskId: 1, isRunning: true, mode: "countdown", sessionLengthMinutes: 10, elapsedSeconds: 10_000 }),
      t
    );
    expect(view.secondsLeft).toBe(0);
  });

  it("defaults to the task's planned minutes when set", () => {
    const t = task({ id: 1, plannedMinutes: 45 });
    const view = computeCountdownView(snapshot(), t);
    expect(view.defaultMinutes).toBe(45);
  });

  it("falls back to 25 minutes when plannedMinutes is unset or zero", () => {
    expect(computeCountdownView(snapshot(), task({ id: 1, plannedMinutes: null })).defaultMinutes).toBe(25);
    expect(computeCountdownView(snapshot(), task({ id: 1, plannedMinutes: 0 })).defaultMinutes).toBe(25);
    expect(computeCountdownView(snapshot(), null).defaultMinutes).toBe(25);
  });
});

describe("computeLiveActualView", () => {
  it("is inactive when no task is given", () => {
    const view = computeLiveActualView(snapshot({ activeTaskId: 1, isRunning: true, mode: "stopwatch" }), null);
    expect(view.isRunningHere).toBe(false);
    expect(view.actualSeconds).toBeNull();
  });

  it("is inactive when the active task is a different one", () => {
    const t = task({ id: 1, actualMinutes: 10 });
    const view = computeLiveActualView(snapshot({ activeTaskId: 2, isRunning: true, mode: "stopwatch" }), t);
    expect(view.isRunningHere).toBe(false);
  });

  it("is inactive during a countdown session for this task (mode gate)", () => {
    const t = task({ id: 1, actualMinutes: 10 });
    const view = computeLiveActualView(snapshot({ activeTaskId: 1, isRunning: true, mode: "countdown" }), t);
    expect(view.isRunningHere).toBe(false);
    expect(view.actualSeconds).toBeNull();
  });

  it("reports live actual seconds during a stopwatch run for this task", () => {
    const t = task({ id: 1, actualMinutes: 10 });
    const view = computeLiveActualView(
      snapshot({ activeTaskId: 1, isRunning: true, mode: "stopwatch", elapsedSeconds: 30 }),
      t
    );
    expect(view.isRunningHere).toBe(true);
    expect(view.actualSeconds).toBe(10 * 60 + 30);
  });
});
