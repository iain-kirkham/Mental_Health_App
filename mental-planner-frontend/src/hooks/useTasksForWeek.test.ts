import { createElement } from "react";
import { describe, expect, it } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  computeReorderedColumn,
  mergeSubtaskChanges,
  mergeTaskChanges,
  useTaskMutation,
} from "./useTasksForWeek";
import type { SubtaskResponseDTO, TaskResponseDTO } from "@/types";

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

describe("mergeTaskChanges", () => {
  it("layers changes on top of the existing task's fields", () => {
    const existing = task({ id: 1, title: "Original", priority: "LOW", plannedMinutes: 30 });

    const merged = mergeTaskChanges(existing, { title: "Updated" });

    expect(merged.title).toBe("Updated");
    expect(merged.priority).toBe("LOW");
    expect(merged.plannedMinutes).toBe(30);
  });
});

describe("mergeSubtaskChanges", () => {
  function subtask(overrides: Partial<SubtaskResponseDTO> & { id: number }): SubtaskResponseDTO {
    return { taskId: 1, title: `Subtask ${overrides.id}`, completed: false, sortOrder: 0, plannedMinutes: null, ...overrides };
  }

  it("layers changes on top of the existing subtask's fields", () => {
    const existing = subtask({ id: 1, title: "Original", sortOrder: 2 });

    const merged = mergeSubtaskChanges(existing, { completed: true });

    expect(merged.completed).toBe(true);
    expect(merged.title).toBe("Original");
    expect(merged.sortOrder).toBe(2);
  });
});

describe("useTaskMutation snapshot timing", () => {
  it("binds the snapshot from the cache as it was before the optimistic patch, not after", async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
    });
    const queryKey = ["tasks", "test"] as const;
    queryClient.setQueryData<TaskResponseDTO[]>(queryKey, [task({ id: 1, title: "before" })]);

    const receivedSnapshots: Array<string | undefined> = [];

    const { result } = renderHook(
      () =>
        useTaskMutation<{ id: number; title: string }, void, string | undefined>(queryClient, queryKey, {
          snapshot: (current, vars) => current?.find((t) => t.id === vars.id)?.title,
          mutationFn: async (_vars, snapshot) => {
            receivedSnapshots.push(snapshot);
          },
          updater: (current, vars) => current.map((t) => (t.id === vars.id ? { ...t, title: vars.title } : t)),
          fallbackMessage: "boom",
        }),
      {
        wrapper: ({ children }) => createElement(QueryClientProvider, { client: queryClient }, children),
      }
    );

    result.current.mutate({ id: 1, title: "after" });

    // The optimistic patch has already landed by the time mutationFn runs...
    await waitFor(() => expect(queryClient.getQueryData<TaskResponseDTO[]>(queryKey)?.[0].title).toBe("after"));
    await waitFor(() => expect(receivedSnapshots).toHaveLength(1));

    // ...but the snapshot bound at `.mutate()` call time still reflects pre-patch state.
    expect(receivedSnapshots[0]).toBe("before");
  });
});
