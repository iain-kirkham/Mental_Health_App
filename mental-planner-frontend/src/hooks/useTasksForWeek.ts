import { useEffect, useMemo, useSyncExternalStore } from "react";
import { useMutation, useQuery, useQueryClient, type QueryClient, type QueryKey } from "@tanstack/react-query";
import { toast } from "sonner";
import {
  archiveTask as apiArchiveTask,
  createSubtask as apiCreateSubtask,
  createTask as apiCreateTask,
  deleteSubtask as apiDeleteSubtask,
  deleteTask as apiDeleteTask,
  getTasksForDateRange as apiGetTasksForDateRange,
  reorderTasks as apiReorderTasks,
  updateCompletion as apiUpdateCompletion,
  updateSubtask as apiUpdateSubtask,
  updateTask as apiUpdateTask,
} from "@/lib/tasks-api";
import { useTimerStore } from "@/store/timerStore";
import { toFriendlyMessage } from "@/lib/connectivity";
import type {
  SubtaskRequestDTO,
  SubtaskResponseDTO,
  TaskPriority,
  TaskReorderItemDTO,
  TaskRequestDTO,
  TaskResponseDTO,
} from "@/types";

function subscribeNoop() {
  return () => {};
}

/** PersistQueryClientProvider's `isRestoring` flag can make useQuery's optimistic result
 * differ between the server render and the client's first hydration pass (it branches on
 * `environmentManager.isServer()` internally). Gating isLoading behind mount status forces
 * both passes to agree on the loading/skeleton state regardless of that internal timing. */
function useHasMounted(): boolean {
  return useSyncExternalStore(subscribeNoop, () => true, () => false);
}

function toRequestDTO(task: TaskResponseDTO): TaskRequestDTO {
  return {
    title: task.title,
    description: task.description,
    scheduledDate: task.scheduledDate,
    startTime: task.startTime,
    endTime: task.endTime,
    completed: task.completed,
    sortOrder: task.sortOrder,
    plannedMinutes: task.plannedMinutes,
    actualMinutes: task.actualMinutes,
    category: task.category,
    archived: task.archived,
    priority: task.priority,
  };
}

/** Shared optimistic-update/rollback plumbing for every task mutation below. Reads/writes
 * always go through the query cache (never a render-time closure) so a mutation that gets
 * queued while offline still acts on the live cache when it actually replays. */
function withOptimisticUpdate<TVars>(
  queryClient: QueryClient,
  queryKey: QueryKey,
  updater: (current: TaskResponseDTO[], vars: TVars) => TaskResponseDTO[],
  fallbackMessage: string
) {
  return {
    onMutate: async (vars: TVars) => {
      await queryClient.cancelQueries({ queryKey });
      const previous = queryClient.getQueryData<TaskResponseDTO[]>(queryKey);
      queryClient.setQueryData<TaskResponseDTO[]>(queryKey, (current = []) => updater(current, vars));
      return { previous };
    },
    onError: (error: unknown, _vars: TVars, context: { previous?: TaskResponseDTO[] } | undefined) => {
      if (context?.previous) queryClient.setQueryData(queryKey, context.previous);
      toast.error(toFriendlyMessage(error, fallbackMessage));
    },
  };
}

/** Computes the destination column for a task move/reorder: `taskId` removed from wherever it
 * was, then reinserted into `destDateKey`'s column (sorted by `sortOrder`) at `destIndex`
 * (clamped to the column's bounds), with its `scheduledDate` patched to `destDateKey`. Returns
 * just that column, in its new order - the moved task's new `sortOrder` is its index within it.
 * Shared by moveTaskMutation's `mutationFn` (needs the reorder payload and the moved task's
 * clamped index) and its optimistic `updater` (needs to patch every affected task's sortOrder
 * in the cache), so the splice/clamp logic has exactly one place to go wrong. */
export function computeReorderedColumn(
  current: TaskResponseDTO[],
  vars: { taskId: number; destDateKey: string; destIndex: number }
): TaskResponseDTO[] {
  const task = current.find((t) => t.id === vars.taskId);
  if (!task) return [];

  const destColumn = current
    .filter((t) => t.scheduledDate === vars.destDateKey && t.id !== vars.taskId)
    .sort((a, b) => a.sortOrder - b.sortOrder);
  const clampedIndex = Math.max(0, Math.min(vars.destIndex, destColumn.length));
  destColumn.splice(clampedIndex, 0, { ...task, scheduledDate: vars.destDateKey });
  return destColumn;
}

/** Every task mutation in this hook shares the same optimistic-update/rollback/mutate wiring -
 * only what actually varies (the request, the optimistic patch, what to reconcile on success)
 * differs per call site. */
function useTaskMutation<TVars, TResult>(
  queryClient: QueryClient,
  queryKey: QueryKey,
  config: {
    mutationFn: (vars: TVars) => Promise<TResult>;
    updater: (current: TaskResponseDTO[], vars: TVars) => TaskResponseDTO[];
    fallbackMessage: string;
    onSuccess?: (result: TResult, vars: TVars) => void;
  }
) {
  return useMutation({
    mutationFn: config.mutationFn,
    ...withOptimisticUpdate<TVars>(queryClient, queryKey, config.updater, config.fallbackMessage),
    onSuccess: config.onSuccess,
  });
}

export default function useTasksForWeek(
  startDate: string,
  endDate: string,
  getToken: () => Promise<string | null>
) {
  const queryClient = useQueryClient();
  const queryKey = useMemo(() => ["tasks", startDate, endDate] as const, [startDate, endDate]);

  const query = useQuery({
    queryKey,
    queryFn: () => apiGetTasksForDateRange(startDate, endDate, getToken),
  });
  const hasMounted = useHasMounted();
  const tasks = query.data ?? [];
  // `query.isPending` stays true while a fetch is paused offline (no error, no data, no
  // progress) - gating the skeleton on that alone left the board stuck "loading" forever when
  // there's no persisted cache to fall back to. `isPending && isFetching` excludes the paused
  // case, so we fall through to an empty board instead of hanging.
  const isLoading = !hasMounted || query.isLoading;

  useEffect(() => {
    if (!hasMounted || !query.isPaused || query.data) return;
    toast.error("You're offline and no cached tasks are available yet. Reconnect to load your tasks.", {
      id: "tasks-offline-no-cache",
    });
  }, [hasMounted, query.isPaused, query.data]);

  useEffect(() => {
    if (!query.isError) return;
    toast.error(toFriendlyMessage(query.error, "Unable to load tasks."));
  }, [query.isError, query.error]);

  // Patches in the freshly-persisted actualMinutes whenever the global task stopwatch (which
  // keeps running/persisting across route navigation) saves a task that's part of this list.
  useEffect(() => {
    return useTimerStore.subscribe((state) => {
      const persisted = state.lastPersistedTask;
      if (!persisted) return;
      queryClient.setQueryData<TaskResponseDTO[]>(queryKey, (current) =>
        current ? current.map((t) => (t.id === persisted.id ? persisted : t)) : current
      );
    });
  }, [queryClient, queryKey]);

  const addTaskMutation = useTaskMutation<{ request: TaskRequestDTO; tempId: number }, TaskResponseDTO>(
    queryClient,
    queryKey,
    {
      mutationFn: (vars) => apiCreateTask(vars.request, getToken),
      updater: (current, vars) => [...current, { ...vars.request, id: vars.tempId, subtasks: [] }],
      fallbackMessage: "Unable to add task.",
      onSuccess: (created, vars) => {
        queryClient.setQueryData<TaskResponseDTO[]>(queryKey, (current = []) =>
          current.map((t) => (t.id === vars.tempId ? created : t))
        );
      },
    }
  );

  const addTask = (
    dateKey: string,
    title: string,
    details?: { description?: string | null; category?: string | null; plannedMinutes?: number | null; priority?: TaskPriority }
  ) => {
    const trimmed = title.trim();
    if (!trimmed) return;

    const currentTasks = queryClient.getQueryData<TaskResponseDTO[]>(queryKey) ?? [];
    const maxSortOrder = currentTasks
      .filter((task) => task.scheduledDate === dateKey)
      .reduce((max, task) => Math.max(max, task.sortOrder), -1);

    const request: TaskRequestDTO = {
      title: trimmed,
      description: details?.description ?? null,
      scheduledDate: dateKey,
      startTime: null,
      endTime: null,
      completed: false,
      sortOrder: maxSortOrder + 1,
      plannedMinutes: details?.plannedMinutes ?? null,
      actualMinutes: 0,
      category: details?.category ?? null,
      archived: false,
      priority: details?.priority ?? "NORMAL",
    };

    addTaskMutation.mutate({ request, tempId: -Date.now() });
  };

  const updateTaskMutation = useTaskMutation<{ id: number; changes: Partial<TaskRequestDTO> }, TaskResponseDTO>(
    queryClient,
    queryKey,
    {
      mutationFn: (vars) => {
        const existing = queryClient.getQueryData<TaskResponseDTO[]>(queryKey)?.find((t) => t.id === vars.id);
        if (!existing) throw new Error("Task no longer exists.");
        const request: TaskRequestDTO = { ...toRequestDTO(existing), ...vars.changes };
        return apiUpdateTask(vars.id, request, getToken);
      },
      updater: (current, vars) => current.map((t) => (t.id === vars.id ? { ...t, ...vars.changes } : t)),
      fallbackMessage: "Unable to update task.",
      onSuccess: (updated) => {
        queryClient.setQueryData<TaskResponseDTO[]>(queryKey, (current = []) =>
          current.map((t) => (t.id === updated.id ? updated : t))
        );
      },
    }
  );

  const updateTask = (id: number, changes: Partial<TaskRequestDTO>) => {
    const existing = queryClient.getQueryData<TaskResponseDTO[]>(queryKey)?.find((t) => t.id === id);
    if (!existing) return;
    updateTaskMutation.mutate({ id, changes });
  };

  // Sets a task's completion and cascades it to all of its subtasks in a single atomic
  // mutation, rather than firing the parent update and one PUT per subtask as separate
  // concurrent requests - those raced against each other and could leave a random subset of
  // subtasks out of sync with the optimistic UI state.
  const setCompletionMutation = useTaskMutation<{ id: number; completed: boolean }, TaskResponseDTO>(
    queryClient,
    queryKey,
    {
      mutationFn: (vars) => apiUpdateCompletion(vars.id, vars.completed, getToken),
      updater: (current, vars) =>
        current.map((t) =>
          t.id === vars.id
            ? { ...t, completed: vars.completed, subtasks: t.subtasks.map((s) => ({ ...s, completed: vars.completed })) }
            : t
        ),
      fallbackMessage: "Unable to update task.",
      onSuccess: (updated) => {
        queryClient.setQueryData<TaskResponseDTO[]>(queryKey, (current = []) =>
          current.map((t) => (t.id === updated.id ? updated : t))
        );
      },
    }
  );

  const setTaskCompletion = (id: number, completed: boolean) => {
    setCompletionMutation.mutate({ id, completed });
  };

  const removeTaskMutation = useTaskMutation<{ id: number }, void>(queryClient, queryKey, {
    mutationFn: (vars) => apiDeleteTask(vars.id, getToken),
    updater: (current, vars) => current.filter((t) => t.id !== vars.id),
    fallbackMessage: "Unable to delete task.",
  });

  const removeTask = (id: number) => {
    removeTaskMutation.mutate({ id });
  };

  // Moves a task to (possibly) a different day column at a given index, and renumbers that
  // column's sortOrder to match the drop position. Kept as a single compound mutation so it
  // queues/replays as one unit while offline.
  const moveTaskMutation = useTaskMutation<
    { taskId: number; sourceDateKey: string; destDateKey: string; destIndex: number },
    TaskResponseDTO[]
  >(queryClient, queryKey, {
    mutationFn: async (vars) => {
      const current = queryClient.getQueryData<TaskResponseDTO[]>(queryKey) ?? [];
      const task = current.find((t) => t.id === vars.taskId);
      if (!task) throw new Error("Task no longer exists.");

      const destColumn = computeReorderedColumn(current, vars);
      const clampedIndex = destColumn.findIndex((t) => t.id === vars.taskId);
      const items: TaskReorderItemDTO[] = destColumn.map((t, index) => ({ id: t.id, sortOrder: index }));

      // Compare against the day the task was on *before* this mutation's optimistic update
      // (captured in vars, at call time) rather than `task.scheduledDate` read back from the
      // cache here - onMutate has already run and written the moved date into the cache by the
      // time mutationFn executes, so that field is always equal to destDateKey and the guard
      // would never fire, silently dropping the scheduledDate change on cross-day moves.
      if (vars.sourceDateKey !== vars.destDateKey) {
        await apiUpdateTask(
          vars.taskId,
          { ...toRequestDTO(task), scheduledDate: vars.destDateKey, sortOrder: clampedIndex },
          getToken
        );
      }
      return apiReorderTasks(items, getToken);
    },
    updater: (current, vars) => {
      const destColumn = computeReorderedColumn(current, vars);
      if (destColumn.length === 0) return current;
      const sortOrderById = new Map(destColumn.map((t, index) => [t.id, index]));

      return current.map((t) => {
        const newSortOrder = sortOrderById.get(t.id);
        if (newSortOrder === undefined) return t;
        return t.id === vars.taskId
          ? { ...t, scheduledDate: vars.destDateKey, sortOrder: newSortOrder }
          : { ...t, sortOrder: newSortOrder };
      });
    },
    fallbackMessage: "Unable to move task.",
    onSuccess: (updatedColumn) => {
      const updatedById = new Map(updatedColumn.map((t) => [t.id, t]));
      queryClient.setQueryData<TaskResponseDTO[]>(queryKey, (current = []) =>
        current.map((t) => updatedById.get(t.id) ?? t)
      );
    },
  });

  const moveTask = (taskId: number, destDateKey: string, destIndex: number) => {
    const task = queryClient.getQueryData<TaskResponseDTO[]>(queryKey)?.find((t) => t.id === taskId);
    if (!task) return;
    moveTaskMutation.mutate({ taskId, sourceDateKey: task.scheduledDate, destDateKey, destIndex });
  };

  const archiveTaskMutation = useTaskMutation<{ id: number }, TaskResponseDTO>(queryClient, queryKey, {
    mutationFn: (vars) => apiArchiveTask(vars.id, getToken),
    updater: (current, vars) => current.filter((t) => t.id !== vars.id),
    fallbackMessage: "Unable to archive task.",
  });

  const archiveTask = (id: number) => {
    archiveTaskMutation.mutate({ id });
  };

  const addSubtaskMutation = useTaskMutation<
    { taskId: number; request: SubtaskRequestDTO; tempId: number },
    SubtaskResponseDTO
  >(queryClient, queryKey, {
    mutationFn: (vars) => apiCreateSubtask(vars.taskId, vars.request, getToken),
    updater: (current, vars) =>
      current.map((t) =>
        t.id === vars.taskId
          ? { ...t, subtasks: [...t.subtasks, { ...vars.request, id: vars.tempId, taskId: vars.taskId }] }
          : t
      ),
    fallbackMessage: "Unable to add subtask.",
    onSuccess: (created, vars) => {
      queryClient.setQueryData<TaskResponseDTO[]>(queryKey, (current = []) =>
        current.map((t) =>
          t.id === vars.taskId ? { ...t, subtasks: t.subtasks.map((s) => (s.id === vars.tempId ? created : s)) } : t
        )
      );
    },
  });

  const addSubtask = (taskId: number, title: string) => {
    const trimmed = title.trim();
    if (!trimmed) return;

    const parent = queryClient.getQueryData<TaskResponseDTO[]>(queryKey)?.find((t) => t.id === taskId);
    if (!parent) return;

    const request: SubtaskRequestDTO = {
      title: trimmed,
      completed: false,
      sortOrder: parent.subtasks.length,
      plannedMinutes: null,
    };

    addSubtaskMutation.mutate({ taskId, request, tempId: -Date.now() });
  };

  const updateSubtaskMutation = useTaskMutation<
    { taskId: number; subtaskId: number; changes: Partial<SubtaskRequestDTO> },
    SubtaskResponseDTO
  >(queryClient, queryKey, {
    mutationFn: (vars) => {
      const parent = queryClient.getQueryData<TaskResponseDTO[]>(queryKey)?.find((t) => t.id === vars.taskId);
      const existing = parent?.subtasks.find((s) => s.id === vars.subtaskId);
      if (!existing) throw new Error("Subtask no longer exists.");
      const request: SubtaskRequestDTO = {
        title: existing.title,
        completed: existing.completed,
        sortOrder: existing.sortOrder,
        plannedMinutes: existing.plannedMinutes,
        ...vars.changes,
      };
      return apiUpdateSubtask(vars.taskId, vars.subtaskId, request, getToken);
    },
    updater: (current, vars) =>
      current.map((t) =>
        t.id === vars.taskId
          ? { ...t, subtasks: t.subtasks.map((s) => (s.id === vars.subtaskId ? { ...s, ...vars.changes } : s)) }
          : t
      ),
    fallbackMessage: "Unable to update subtask.",
    onSuccess: (updated, vars) => {
      queryClient.setQueryData<TaskResponseDTO[]>(queryKey, (current = []) =>
        current.map((t) =>
          t.id === vars.taskId
            ? { ...t, subtasks: t.subtasks.map((s) => (s.id === vars.subtaskId ? updated : s)) }
            : t
        )
      );
    },
  });

  const updateSubtaskItem = (taskId: number, subtaskId: number, changes: Partial<SubtaskRequestDTO>) => {
    updateSubtaskMutation.mutate({ taskId, subtaskId, changes });
  };

  const removeSubtaskMutation = useTaskMutation<{ taskId: number; subtaskId: number }, void>(
    queryClient,
    queryKey,
    {
      mutationFn: (vars) => apiDeleteSubtask(vars.taskId, vars.subtaskId, getToken),
      updater: (current, vars) =>
        current.map((t) =>
          t.id === vars.taskId ? { ...t, subtasks: t.subtasks.filter((s) => s.id !== vars.subtaskId) } : t
        ),
      fallbackMessage: "Unable to delete subtask.",
    }
  );

  const removeSubtaskItem = (taskId: number, subtaskId: number) => {
    removeSubtaskMutation.mutate({ taskId, subtaskId });
  };

  return {
    tasks,
    isLoading,
    addTask,
    updateTask,
    setTaskCompletion,
    removeTask,
    moveTask,
    archiveTask,
    addSubtask,
    updateSubtaskItem,
    removeSubtaskItem,
  };
}
