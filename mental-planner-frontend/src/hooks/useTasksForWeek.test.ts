import { describe, expect, it } from "vitest";
import { computeReorderedColumn } from "./useTasksForWeek";
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

describe("computeReorderedColumn", () => {
  it("reorders within the same day column", () => {
    const current = [
      task({ id: 1, scheduledDate: "day1", sortOrder: 0 }),
      task({ id: 2, scheduledDate: "day1", sortOrder: 1 }),
      task({ id: 3, scheduledDate: "day1", sortOrder: 2 }),
    ];

    const result = computeReorderedColumn(current, { taskId: 3, destDateKey: "day1", destIndex: 0 });

    expect(result.map((t) => t.id)).toEqual([3, 1, 2]);
  });

  it("moves a task into a different day column at the given index", () => {
    const current = [
      task({ id: 1, scheduledDate: "day1", sortOrder: 0 }),
      task({ id: 2, scheduledDate: "day2", sortOrder: 0 }),
      task({ id: 3, scheduledDate: "day2", sortOrder: 1 }),
    ];

    const result = computeReorderedColumn(current, { taskId: 1, destDateKey: "day2", destIndex: 1 });

    expect(result.map((t) => t.id)).toEqual([2, 1, 3]);
    expect(result.find((t) => t.id === 1)?.scheduledDate).toBe("day2");
  });

  it("clamps destIndex above the column length to the end", () => {
    const current = [
      task({ id: 1, scheduledDate: "day1", sortOrder: 0 }),
      task({ id: 2, scheduledDate: "day2", sortOrder: 0 }),
    ];

    const result = computeReorderedColumn(current, { taskId: 1, destDateKey: "day2", destIndex: 99 });

    expect(result.map((t) => t.id)).toEqual([2, 1]);
  });

  it("clamps a negative destIndex to the start", () => {
    const current = [
      task({ id: 1, scheduledDate: "day1", sortOrder: 0 }),
      task({ id: 2, scheduledDate: "day2", sortOrder: 0 }),
    ];

    const result = computeReorderedColumn(current, { taskId: 1, destDateKey: "day2", destIndex: -5 });

    expect(result.map((t) => t.id)).toEqual([1, 2]);
  });

  it("returns an empty array when the task no longer exists", () => {
    const current = [task({ id: 1, scheduledDate: "day1", sortOrder: 0 })];

    const result = computeReorderedColumn(current, { taskId: 999, destDateKey: "day1", destIndex: 0 });

    expect(result).toEqual([]);
  });
});
