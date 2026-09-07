import { create } from "zustand";
import { persist } from "zustand/middleware";
import { format } from "date-fns";
import type { TaskResponseDTO, TimeEntrySource } from "@/types";

type PersistFn = (taskId: number, actualMinutes: number) => void;
type LoggedTimeEntry = {
  taskId: number | null;
  runKey: string;
  startedAt: string;
  endedAt: string;
  minutes: number;
  entryDate: string;
  source: TimeEntrySource;
};
type LogEntryFn = (entry: LoggedTimeEntry) => void;
type TimerMode = "stopwatch" | "focus";

type CompletedSession = {
  taskId: number | null;
  runKey: string | null;
  durationMinutes: number;
  startedAt: string | null;
};

interface TimerState {
  mode: TimerMode;
  activeTaskId: number | null;
  /** Only meaningful in focus mode - the configured session length. */
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
  /** ISO timestamp of when the current run was first started (survives pause/resume, cleared on
   * cancel/stop/complete). Used as the logged FOCUS entry's startedAt once saved (focus mode),
   * and as a stopwatch-mode run's logged time entry startedAt. */
  firstStartedAt: string | null;
  /** Identifies this run for the write-immediately-then-reflect handoff (see focusEntryHandoff) -
   * a fresh id per run so a fast second run's reflection can't be attached to the first run's
   * entry. Cleared alongside firstStartedAt. */
  runKey: string | null;
  /** actualMinutes the task had at the moment this stopwatch run first started (survives pause/
   * resume, unlike baseActualMinutes which advances on each resume). The difference between the
   * task's actualMinutes when the run ends and this value is the run's logged entry duration. */
  runStartActualMinutes: number | null;
  intervalId: ReturnType<typeof setInterval> | null;
  /** Registered once by TimerStoreBridge (mounted app-wide), since the store itself has no auth/API access. */
  persistFn: PersistFn | null;
  /** Registered once by TimerStoreBridge, logs a completed stopwatch/focus run as a history entry. */
  logEntryFn: LogEntryFn | null;
  /** Bumped after a successful persist so any mounted task list can patch its local copy. */
  lastPersistedTask: TaskResponseDTO | null;
  /** Set when a focus session finishes on its own (not cancelled), so the session-reflection
   * form can react - the store clears its own session fields as part of the same completion, so
   * this is the only place that data is still available by the time the form is shown. */
  lastCompletedSession: CompletedSession | null;
  setPersistFn: (fn: PersistFn) => void;
  setLogEntryFn: (fn: LogEntryFn) => void;
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
  /** Discards the current run without persisting - used by the focus-mode Reset/Cancel control.
   * Progress already saved by an earlier pause is unaffected; only time since the last pause is lost. */
  cancelTimer: () => void;
  tick: () => void;
}

function generateRunKey(): string {
  return typeof crypto !== "undefined" && "randomUUID" in crypto
    ? crypto.randomUUID()
    : `run-${Date.now()}-${Math.random().toString(36).slice(2)}`;
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

/** Logs the just-ended run (start to stop/switch/complete, spanning any internal pauses) as one
 * history entry, tagged STOPWATCH or FOCUS to match how it was run - regardless of whether a
 * task is linked, since a Focus session can run task-less. actualMinutes itself is kept correct
 * by persistElapsed separately (task-linked runs only) - this only records when the run
 * happened, so logging is skipped when the run details (run start, or how much it added)
 * aren't available. */
function logStopwatchRunEntry(
  logEntryFn: LogEntryFn | null,
  mode: TimerMode,
  activeTaskId: number | null,
  runKey: string | null,
  firstStartedAt: string | null,
  runStartActualMinutes: number | null,
  finalActualMinutes: number
) {
  if (!logEntryFn || runKey === null) return;
  if (firstStartedAt === null || runStartActualMinutes === null) return;
  const entryMinutes = finalActualMinutes - runStartActualMinutes;
  if (entryMinutes <= 0) return;
  logEntryFn({
    taskId: activeTaskId,
    runKey,
    startedAt: firstStartedAt,
    endedAt: new Date().toISOString(),
    minutes: entryMinutes,
    entryDate: format(new Date(firstStartedAt), "yyyy-MM-dd"),
    source: mode === "focus" ? "FOCUS" : "STOPWATCH",
  });
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
      runKey: null,
      runStartActualMinutes: null,
      intervalId: null,
      persistFn: null,
      logEntryFn: null,
      lastPersistedTask: null,
      lastCompletedSession: null,

      setPersistFn: (fn) => set({ persistFn: fn }),
      setLogEntryFn: (fn) => set({ logEntryFn: fn }),
      setLastPersistedTask: (task) => set({ lastPersistedTask: task }),

      tick: () => {
        const state = get();
        const elapsedSeconds = computeElapsed(state.startedAt, state.elapsedWhenPaused);

        if (
          state.mode === "focus" &&
          state.sessionLengthMinutes !== null &&
          elapsedSeconds >= state.sessionLengthMinutes * 60
        ) {
          if (state.intervalId) clearInterval(state.intervalId);
          const cappedElapsed = state.sessionLengthMinutes * 60;
          const finalMinutes = state.baseActualMinutes + Math.round(cappedElapsed / 60);
          persistElapsed(state.persistFn, state.activeTaskId, cappedElapsed, state.baseActualMinutes);
          logStopwatchRunEntry(
            state.logEntryFn,
            state.mode,
            state.activeTaskId,
            state.runKey,
            state.firstStartedAt,
            state.runStartActualMinutes,
            finalMinutes
          );
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
            runKey: null,
            runStartActualMinutes: null,
            lastCompletedSession: {
              taskId: state.activeTaskId,
              runKey: state.runKey,
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
          // Resuming the run we just paused: baseActualMinutes was fixed at this run's true
          // start and pauseTimer never touches it, so it's still correct - deliberately not
          // re-derived from the caller's actualMinutes prop, which may still be stale if the
          // previous pause's persist request (an async GET+PUT round trip) hasn't resolved yet.
          // elapsedWhenPaused carries forward the cumulative elapsed-so-far (already the value
          // pauseTimer left in elapsedSeconds) so the display doesn't jump, and so completion's
          // baseActualMinutes + round(elapsedSeconds/60) counts each segment exactly once.
          const intervalId = setInterval(() => get().tick(), 1000);
          set({
            startedAt: Date.now(),
            elapsedWhenPaused: state.elapsedSeconds,
            isRunning: true,
            intervalId,
          });
          return;
        }

        // Switching from another running task/session: persist its elapsed time first, and log
        // the run that's ending (if it had a run in progress) as a history entry.
        if (state.isRunning && (state.activeTaskId !== null || state.runKey !== null)) {
          const finalMinutes = state.baseActualMinutes + Math.round(state.elapsedSeconds / 60);
          persistElapsed(state.persistFn, state.activeTaskId, state.elapsedSeconds, state.baseActualMinutes);
          logStopwatchRunEntry(
            state.logEntryFn,
            state.mode,
            state.activeTaskId,
            state.runKey,
            state.firstStartedAt,
            state.runStartActualMinutes,
            finalMinutes
          );
        }

        const intervalId = setInterval(() => get().tick(), 1000);
        set({
          mode,
          activeTaskId: taskId,
          sessionLengthMinutes: mode === "focus" ? (options?.sessionLengthMinutes ?? null) : null,
          startedAt: Date.now(),
          elapsedWhenPaused: 0,
          elapsedSeconds: 0,
          baseActualMinutes: currentActualMinutes,
          isRunning: true,
          intervalId,
          firstStartedAt: new Date().toISOString(),
          runKey: generateRunKey(),
          runStartActualMinutes: currentActualMinutes,
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
        const finalMinutes = state.baseActualMinutes + Math.round(elapsedSeconds / 60);
        persistElapsed(state.persistFn, state.activeTaskId, elapsedSeconds, state.baseActualMinutes);
        logStopwatchRunEntry(
          state.logEntryFn,
          state.mode,
          state.activeTaskId,
          state.runKey,
          state.firstStartedAt,
          state.runStartActualMinutes,
          finalMinutes
        );
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
          runKey: null,
          runStartActualMinutes: null,
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
          runKey: null,
          runStartActualMinutes: null,
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
        runKey: state.runKey,
        runStartActualMinutes: state.runStartActualMinutes,
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
  // Runs the completion check immediately in case the session finished (or a focus session ran
  // past its length) while the page was closed, rather than waiting up to a second for it.
  useTimerStore.getState().tick();
  if (!useTimerStore.getState().isRunning) return;
  const intervalId = setInterval(() => useTimerStore.getState().tick(), 1000);
  useTimerStore.setState({ intervalId });
}
