import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { TaskResponseDTO } from "@/types";

type PersistFn = (taskId: number, actualMinutes: number) => void;
type TimerMode = "stopwatch" | "countdown";

type CompletedSession = {
  taskId: number | null;
  durationMinutes: number;
  startedAt: string | null;
};

interface TimerState {
  mode: TimerMode;
  activeTaskId: number | null;
  /** Only meaningful in countdown mode - the configured session length. */
  sessionLengthMinutes: number | null;
  /** Timestamp (ms) the current running segment began, or null when paused/stopped. Elapsed
   * time is derived from this rather than incremented tick-by-tick, so it stays accurate even
   * when the interval is throttled by a backgrounded/inactive browser tab. */
  startedAt: number | null;
  /** Seconds accumulated from segments before the current one (i.e. across pause/resume). */
  elapsedWhenPaused: number;
  /** Derived display value, recomputed every tick (and after rehydrate) from startedAt/elapsedWhenPaused. */
  elapsedSeconds: number;
  isRunning: boolean;
  /** actualMinutes the task already had when this run started, used to compute the total to persist. */
  baseActualMinutes: number;
  /** ISO timestamp of when the current countdown session was first started (survives pause/resume,
   * cleared on cancel/stop/complete). Used as the PomodoroSession's startTime once saved. */
  firstStartedAt: string | null;
  intervalId: ReturnType<typeof setInterval> | null;
  /** Registered once by TimerStoreBridge (mounted app-wide), since the store itself has no auth/API access. */
  persistFn: PersistFn | null;
  /** Bumped after a successful persist so any mounted task list can patch its local copy. */
  lastPersistedTask: TaskResponseDTO | null;
  /** Set when a countdown session finishes on its own (not cancelled), so the session-reflection
   * form can react - the store clears its own session fields as part of the same completion, so
   * this is the only place that data is still available by the time the form is shown. */
  lastCompletedSession: CompletedSession | null;
  setPersistFn: (fn: PersistFn) => void;
  setLastPersistedTask: (task: TaskResponseDTO) => void;
  startTimer: (
    taskId: number | null,
    currentActualMinutes: number,
    options?: { mode?: TimerMode; sessionLengthMinutes?: number }
  ) => void;
  pauseTimer: () => void;
  /** Stops and persists whatever has accumulated - used when a run is deliberately ended (not
   * currently wired to any UI, but kept as the "stop and keep the time" counterpart to cancelTimer). */
  stopTimer: () => void;
  /** Discards the current run without persisting - used by the countdown Reset/Cancel control.
   * Progress already saved by an earlier pause is unaffected; only time since the last pause is lost. */
  cancelTimer: () => void;
  tick: () => void;
}

function computeElapsed(startedAt: number | null, elapsedWhenPaused: number) {
  if (startedAt === null) return elapsedWhenPaused;
  return elapsedWhenPaused + Math.floor((Date.now() - startedAt) / 1000);
}

function persistElapsed(
  persistFn: PersistFn | null,
  activeTaskId: number | null,
  elapsedSeconds: number,
  baseActualMinutes: number
) {
  if (activeTaskId === null || !persistFn) return;
  const addedMinutes = Math.round(elapsedSeconds / 60);
  if (addedMinutes > 0) {
    persistFn(activeTaskId, baseActualMinutes + addedMinutes);
  }
}

export const useTimerStore = create<TimerState>()(
  persist(
    (set, get) => ({
      mode: "stopwatch",
      activeTaskId: null,
      sessionLengthMinutes: null,
      startedAt: null,
      elapsedWhenPaused: 0,
      elapsedSeconds: 0,
      isRunning: false,
      baseActualMinutes: 0,
      firstStartedAt: null,
      intervalId: null,
      persistFn: null,
      lastPersistedTask: null,
      lastCompletedSession: null,

      setPersistFn: (fn) => set({ persistFn: fn }),
      setLastPersistedTask: (task) => set({ lastPersistedTask: task }),

      tick: () => {
        const state = get();
        const elapsedSeconds = computeElapsed(state.startedAt, state.elapsedWhenPaused);

        if (
          state.mode === "countdown" &&
          state.sessionLengthMinutes !== null &&
          elapsedSeconds >= state.sessionLengthMinutes * 60
        ) {
          if (state.intervalId) clearInterval(state.intervalId);
          const cappedElapsed = state.sessionLengthMinutes * 60;
          persistElapsed(state.persistFn, state.activeTaskId, cappedElapsed, state.baseActualMinutes);
          set({
            activeTaskId: null,
            sessionLengthMinutes: null,
            startedAt: null,
            elapsedWhenPaused: 0,
            elapsedSeconds: 0,
            isRunning: false,
            intervalId: null,
            baseActualMinutes: 0,
            firstStartedAt: null,
            lastCompletedSession: {
              taskId: state.activeTaskId,
              durationMinutes: Math.round(cappedElapsed / 60),
              startedAt: state.firstStartedAt,
            },
          });
          return;
        }

        set({ elapsedSeconds });
      },

      startTimer: (taskId, currentActualMinutes, options) => {
        const state = get();
        if (state.intervalId) clearInterval(state.intervalId);
        const mode = options?.mode ?? "stopwatch";

        if (state.activeTaskId === taskId && state.mode === mode) {
          // Resuming the run we just paused: keep accumulating from our own last-known total
          // rather than trusting the caller's actualMinutes prop, which may still be stale if
          // the previous pause's persist request (an async GET+PUT round trip) hasn't resolved
          // yet - using the stale prop here would silently drop or duplicate time on quick
          // pause/resume toggles.
          const resumedBase = state.baseActualMinutes + Math.round(state.elapsedSeconds / 60);
          const intervalId = setInterval(() => get().tick(), 1000);
          set({
            startedAt: Date.now(),
            elapsedWhenPaused: state.elapsedSeconds,
            baseActualMinutes: resumedBase,
            isRunning: true,
            intervalId,
          });
          return;
        }

        // Switching from another running task/session: persist its elapsed time first.
        if (state.isRunning && state.activeTaskId !== null) {
          persistElapsed(state.persistFn, state.activeTaskId, state.elapsedSeconds, state.baseActualMinutes);
        }

        const intervalId = setInterval(() => get().tick(), 1000);
        set({
          mode,
          activeTaskId: taskId,
          sessionLengthMinutes: mode === "countdown" ? (options?.sessionLengthMinutes ?? null) : null,
          startedAt: Date.now(),
          elapsedWhenPaused: 0,
          elapsedSeconds: 0,
          baseActualMinutes: currentActualMinutes,
          isRunning: true,
          intervalId,
          firstStartedAt: mode === "countdown" ? new Date().toISOString() : null,
        });
      },

      pauseTimer: () => {
        const state = get();
        if (state.intervalId) clearInterval(state.intervalId);
        const elapsedSeconds = computeElapsed(state.startedAt, state.elapsedWhenPaused);
        persistElapsed(state.persistFn, state.activeTaskId, elapsedSeconds, state.baseActualMinutes);
        set({ startedAt: null, elapsedWhenPaused: elapsedSeconds, elapsedSeconds, isRunning: false, intervalId: null });
      },

      stopTimer: () => {
        const state = get();
        if (state.intervalId) clearInterval(state.intervalId);
        const elapsedSeconds = computeElapsed(state.startedAt, state.elapsedWhenPaused);
        persistElapsed(state.persistFn, state.activeTaskId, elapsedSeconds, state.baseActualMinutes);
        set({
          activeTaskId: null,
          mode: "stopwatch",
          sessionLengthMinutes: null,
          startedAt: null,
          elapsedWhenPaused: 0,
          elapsedSeconds: 0,
          isRunning: false,
          intervalId: null,
          baseActualMinutes: 0,
          firstStartedAt: null,
        });
      },

      cancelTimer: () => {
        const state = get();
        if (state.intervalId) clearInterval(state.intervalId);
        set({
          activeTaskId: null,
          mode: "stopwatch",
          sessionLengthMinutes: null,
          startedAt: null,
          elapsedWhenPaused: 0,
          elapsedSeconds: 0,
          isRunning: false,
          intervalId: null,
          baseActualMinutes: 0,
          firstStartedAt: null,
        });
      },
    }),
    {
      name: "task-timer-state-v1",
      // Rehydrated explicitly by TimerStoreBridge once mounted client-side - localStorage isn't
      // available during SSR, and reading it eagerly would cause a hydration mismatch against
      // the server-rendered defaults.
      skipHydration: true,
      partialize: (state) => ({
        mode: state.mode,
        activeTaskId: state.activeTaskId,
        sessionLengthMinutes: state.sessionLengthMinutes,
        startedAt: state.startedAt,
        elapsedWhenPaused: state.elapsedWhenPaused,
        elapsedSeconds: state.elapsedSeconds,
        isRunning: state.isRunning,
        baseActualMinutes: state.baseActualMinutes,
        firstStartedAt: state.firstStartedAt,
      }),
      onRehydrateStorage: () => () => {
        resumeTickingIfNeeded();
      },
    }
  )
);

/** intervalId never survives persistence (it's excluded from partialize, and wouldn't be valid
 * across a reload anyway), so a run that was mid-flight when the page closed comes back out of
 * storage with isRunning true but nothing actually ticking. Restarts that interval and refreshes
 * elapsedSeconds to account for time passed while the page was closed. */
function resumeTickingIfNeeded() {
  if (!useTimerStore.getState().isRunning) return;
  // Runs the completion check immediately in case the session finished (or a countdown ran
  // past its length) while the page was closed, rather than waiting up to a second for it.
  useTimerStore.getState().tick();
  if (!useTimerStore.getState().isRunning) return;
  const intervalId = setInterval(() => useTimerStore.getState().tick(), 1000);
  useTimerStore.setState({ intervalId });
}
