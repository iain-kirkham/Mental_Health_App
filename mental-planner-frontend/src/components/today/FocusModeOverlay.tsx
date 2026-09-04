'use client'

import { useEffect, useRef, useState } from 'react'
import * as DialogPrimitive from '@radix-ui/react-dialog'
import { Pause, Play, Square, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { TimerDisplay } from '@/components/TimerDisplay'
import { useTimerStore } from '@/store/timerStore'
import type { TaskResponseDTO } from '@/types'

const PRESETS_MINUTES = [10, 25, 50]

interface FocusModeOverlayProps {
  task: TaskResponseDTO | null
  onOpenChange: (open: boolean) => void
}

export default function FocusModeOverlay({ task, onOpenChange }: FocusModeOverlayProps) {
  const activeTaskId = useTimerStore((state) => state.activeTaskId)
  const isRunning = useTimerStore((state) => state.isRunning)
  const timerMode = useTimerStore((state) => state.mode)
  const sessionLengthMinutes = useTimerStore((state) => state.sessionLengthMinutes)
  const elapsedSeconds = useTimerStore((state) => state.elapsedSeconds)
  const startTimer = useTimerStore((state) => state.startTimer)
  const pauseTimer = useTimerStore((state) => state.pauseTimer)
  const cancelTimer = useTimerStore((state) => state.cancelTimer)

  // Keyed by task id since this overlay stays mounted across different tasks (task changes via
  // prop, no remount) - a bare useState would leak the previous task's manual choice forward.
  const [manualPreset, setManualPreset] = useState<{ taskId: number; minutes: number } | null>(null)

  const isThisTask = task !== null && activeTaskId === task.id && timerMode === 'countdown'
  const hasSession = isThisTask && sessionLengthMinutes !== null
  const running = isRunning && isThisTask

  // A session that was active for this task and then vanished (completed or cancelled) means the
  // countdown's own lifecycle ended it - the reflection prompt (GlobalPomodoroModal) takes over
  // from here, so this overlay should step aside rather than sit open showing a reset 0:00 ring.
  const hadSessionRef = useRef(false)
  useEffect(() => {
    if (hadSessionRef.current && !hasSession) {
      onOpenChange(false)
    }
    hadSessionRef.current = hasSession
  }, [hasSession, onOpenChange])

  if (!task) return null

  // Defaults the session length to the card's own planned time instead of a flat 25m.
  const defaultMinutes = task.plannedMinutes && task.plannedMinutes > 0 ? task.plannedMinutes : 25
  const selectedPreset = manualPreset && manualPreset.taskId === task.id ? manualPreset.minutes : defaultMinutes

  const totalTime = hasSession ? sessionLengthMinutes * 60 : selectedPreset * 60
  const timeLeft = hasSession ? Math.max(0, totalTime - elapsedSeconds) : totalTime

  const startPause = () => {
    if (running) {
      pauseTimer()
      return
    }
    startTimer(task.id, task.actualMinutes, {
      mode: 'countdown',
      sessionLengthMinutes: hasSession ? sessionLengthMinutes! : selectedPreset,
    })
  }

  return (
    <DialogPrimitive.Root open onOpenChange={(open) => !open && onOpenChange(false)}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-background/97 backdrop-blur-sm data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:animate-in data-[state=open]:fade-in-0" />
        <DialogPrimitive.Content
          className="fixed inset-0 z-50 flex flex-col items-center justify-center gap-12 px-6 outline-none data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 data-[state=open]:animate-in data-[state=open]:fade-in-0 data-[state=open]:zoom-in-95"
          onEscapeKeyDown={() => onOpenChange(false)}
        >
          <DialogPrimitive.Title className="sr-only">Focus on {task.title || 'this task'}</DialogPrimitive.Title>

          <DialogPrimitive.Close
            className="absolute right-6 top-6 rounded-full p-2.5 text-muted-foreground opacity-70 transition-opacity hover:bg-accent hover:opacity-100 focus-visible:opacity-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            aria-label="Exit focus mode"
          >
            <X className="h-5 w-5" />
          </DialogPrimitive.Close>

          <div className="flex max-w-md flex-col items-center gap-2 text-center">
            <span className="text-xs font-medium tracking-normal text-lavender">Focus</span>
            <h2 className="text-2xl font-semibold leading-snug text-foreground">{task.title || 'Untitled task'}</h2>
            {task.category && <span className="text-sm text-muted-foreground">#{task.category}</span>}
          </div>

          <TimerDisplay timeLeft={timeLeft} totalTime={totalTime} isRunning={running} />

          <div className="flex flex-col items-center gap-5">
            {!hasSession && (
              <div className="flex items-center gap-1 rounded-full bg-muted/60 p-1" role="group" aria-label="Session length">
                {PRESETS_MINUTES.map((minutes) => (
                  <button
                    key={minutes}
                    type="button"
                    onClick={() => setManualPreset({ taskId: task.id, minutes })}
                    className={cn(
                      'rounded-full px-4 py-1.5 font-mono text-sm tabular-nums transition-colors',
                      selectedPreset === minutes
                        ? 'bg-lavender text-background'
                        : 'text-muted-foreground hover:text-foreground'
                    )}
                  >
                    {minutes}m
                  </button>
                ))}
              </div>
            )}

            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={startPause}
                className="flex items-center gap-2 rounded-full bg-lavender px-6 py-2.5 text-sm font-semibold text-background transition-opacity hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
              >
                {running ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
                {running ? 'Pause' : hasSession ? 'Resume' : 'Start focus session'}
              </button>
              {hasSession && (
                <button
                  type="button"
                  onClick={() => cancelTimer()}
                  className="flex items-center gap-2 rounded-full border border-border px-4 py-2.5 text-sm font-medium text-muted-foreground transition-colors hover:border-destructive/40 hover:text-destructive focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
                  aria-label="End focus session"
                >
                  <Square className="h-3.5 w-3.5" />
                  End
                </button>
              )}
            </div>
          </div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  )
}
