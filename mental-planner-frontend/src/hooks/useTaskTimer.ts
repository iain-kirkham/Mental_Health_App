'use client'

import { useTimerStore } from '@/store/timerStore'
import type { TaskResponseDTO } from '@/types'

/** The subset of timerStore fields the pure view functions below need - kept as an explicit
 * type (rather than importing the store's own state type) so the pure functions can be tested
 * against a plain object, with no store/render involved. */
export interface TimerSnapshot {
  activeTaskId: number | null
  isRunning: boolean
  mode: 'stopwatch' | 'countdown'
  sessionLengthMinutes: number | null
  elapsedSeconds: number
}

export interface CountdownView {
  /** The active countdown session (if any) belongs to this task. */
  isActiveHere: boolean
  /** Ticking right now, as opposed to this task's session being active but paused. */
  running: boolean
  /** null whenever !isActiveHere; otherwise seconds remaining, floored at 0. */
  secondsLeft: number | null
  /** From the task's own planned time, falling back to 25m - always available regardless of
   * whether a session is currently active, since it's what a not-yet-started session would use. */
  defaultMinutes: number
}

export interface LiveActualView {
  /** A stopwatch run (not a countdown session) is active for this task. */
  isRunningHere: boolean
  /** null whenever !isRunningHere; otherwise the task's stored actualMinutes plus elapsed ticking. */
  actualSeconds: number | null
}

export function computeCountdownView(snapshot: TimerSnapshot, task: TaskResponseDTO | null): CountdownView {
  const isActiveHere = task !== null && snapshot.activeTaskId === task.id && snapshot.mode === 'countdown'
  const running = snapshot.isRunning && isActiveHere
  const secondsLeft =
    isActiveHere && snapshot.sessionLengthMinutes !== null
      ? Math.max(0, snapshot.sessionLengthMinutes * 60 - snapshot.elapsedSeconds)
      : null
  const defaultMinutes = task?.plannedMinutes && task.plannedMinutes > 0 ? task.plannedMinutes : 25
  return { isActiveHere, running, secondsLeft, defaultMinutes }
}

export function computeLiveActualView(snapshot: TimerSnapshot, task: TaskResponseDTO | null): LiveActualView {
  const isRunningHere =
    task !== null && snapshot.activeTaskId === task.id && snapshot.isRunning && snapshot.mode === 'stopwatch'
  const actualSeconds = isRunningHere ? task.actualMinutes * 60 + snapshot.elapsedSeconds : null
  return { isRunningHere, actualSeconds }
}

function useTimerSnapshot(): TimerSnapshot {
  const activeTaskId = useTimerStore((state) => state.activeTaskId)
  const isRunning = useTimerStore((state) => state.isRunning)
  const mode = useTimerStore((state) => state.mode)
  const sessionLengthMinutes = useTimerStore((state) => state.sessionLengthMinutes)
  const elapsedSeconds = useTimerStore((state) => state.elapsedSeconds)
  return { activeTaskId, isRunning, mode, sessionLengthMinutes, elapsedSeconds }
}

export function useCountdownSession(task: TaskResponseDTO | null): CountdownView {
  return computeCountdownView(useTimerSnapshot(), task)
}

export function useLiveActualSeconds(task: TaskResponseDTO | null): LiveActualView {
  return computeLiveActualView(useTimerSnapshot(), task)
}
