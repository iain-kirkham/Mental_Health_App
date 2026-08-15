import { create } from "zustand";
import type { TaskResponseDTO } from "@/types";

type PersistFn = (taskId: number, actualMinutes: number) => void;

interface TimerState {
  activeTaskId: number | null;
  elapsedSeconds: number;
  isRunning: boolean;
  /** actualMinutes the task already had when this run started, used to compute the total to persist. */
  baseActualMinutes: number;
  intervalId: ReturnType<typeof setInterval> | null;
  /** Registered once by TimerStoreBridge (mounted app-wide), since the store itself has no auth/API access. */
  persistFn: PersistFn | null;
  /** Bumped after a successful persist so any mounted task list can patch its local copy. */
  lastPersistedTask: TaskResponseDTO | null;
  setPersistFn: (fn: PersistFn) => void;
  setLastPersistedTask: (task: TaskResponseDTO) => void;
  startTimer: (taskId: number, currentActualMinutes: number) => void;
  pauseTimer: () => void;
  stopTimer: () => void;
  tick: () => void;
}

function persistElapsed(state: TimerState) {
  if (state.activeTaskId === null || !state.persistFn) return;
  const addedMinutes = Math.round(state.elapsedSeconds / 60);
  if (addedMinutes > 0) {
    state.persistFn(state.activeTaskId, state.baseActualMinutes + addedMinutes);
  }
}

export const useTimerStore = create<TimerState>((set, get) => ({
  activeTaskId: null,
  elapsedSeconds: 0,
  isRunning: false,
  baseActualMinutes: 0,
  intervalId: null,
  persistFn: null,
  lastPersistedTask: null,

  setPersistFn: (fn) => set({ persistFn: fn }),
  setLastPersistedTask: (task) => set({ lastPersistedTask: task }),

  tick: () => set((state) => ({ elapsedSeconds: state.elapsedSeconds + 1 })),

  startTimer: (taskId, currentActualMinutes) => {
    const state = get();
    if (state.intervalId) clearInterval(state.intervalId);

    if (state.activeTaskId === taskId) {
      // Resuming the task we just paused: keep accumulating from our own last-known total
      // rather than trusting the caller's actualMinutes prop, which may still be stale if
      // the previous pause's persist request (an async GET+PUT round trip) hasn't resolved
      // yet - using the stale prop here would silently drop or duplicate time on quick
      // pause/resume toggles.
      const resumedBase = state.baseActualMinutes + Math.round(state.elapsedSeconds / 60);
      const intervalId = setInterval(() => get().tick(), 1000);
      set({ elapsedSeconds: 0, baseActualMinutes: resumedBase, isRunning: true, intervalId });
      return;
    }

    // Switching from another running task: persist its elapsed time first.
    if (state.isRunning && state.activeTaskId !== null) {
      persistElapsed(state);
    }

    const intervalId = setInterval(() => get().tick(), 1000);
    set({
      activeTaskId: taskId,
      elapsedSeconds: 0,
      baseActualMinutes: currentActualMinutes,
      isRunning: true,
      intervalId,
    });
  },

  pauseTimer: () => {
    const state = get();
    if (state.intervalId) clearInterval(state.intervalId);
    persistElapsed(state);
    set({ isRunning: false, intervalId: null });
  },

  stopTimer: () => {
    const state = get();
    if (state.intervalId) clearInterval(state.intervalId);
    persistElapsed(state);
    set({ activeTaskId: null, elapsedSeconds: 0, isRunning: false, intervalId: null, baseActualMinutes: 0 });
  },
}));
