'use client'

import { useEffect, useRef, useState, type FormEvent } from 'react'
import * as DialogPrimitive from '@radix-ui/react-dialog'
import { CheckCircle2, Circle, PanelLeftClose, PanelLeftOpen, Pause, Play, Plus, Square, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Textarea } from '@/components/ui/textarea'
import { channelPillClass } from '@/lib/channel-color'
import { useTimerStore } from '@/store/timerStore'
import type { TaskResponseDTO } from '@/types'

const PRESETS_MINUTES = [10, 25, 50]
const NOTES_SAVE_DEBOUNCE_MS = 600

interface ExecutionModeOverlayProps {
  open: boolean
  /** Today's full task list (completed included) - the left queue shows all of it, checking off
   * completed ones, while the center stage and keyboard cycling only ever land on open tasks. */
  tasks: TaskResponseDTO[]
  /** Task to land on when the overlay opens (e.g. a per-task "Focus" button), instead of
   * defaulting to the first open task in the queue. */
  initialTaskId?: number | null
  onOpenChange: (open: boolean) => void
  onToggleComplete: (id: number, completed: boolean) => void
  onToggleSubtask?: (taskId: number, subtaskId: number, completed: boolean) => void
  onAddSubtask?: (taskId: number, title: string) => void
  onUpdateNotes?: (taskId: number, notes: string) => void
}

function formatDigitalTime(totalSeconds: number) {
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  return `${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

export default function ExecutionModeOverlay({
  open,
  tasks,
  initialTaskId = null,
  onOpenChange,
  onToggleComplete,
  onToggleSubtask,
  onAddSubtask,
  onUpdateNotes,
}: ExecutionModeOverlayProps) {
  const activeTaskId = useTimerStore((state) => state.activeTaskId)
  const isRunning = useTimerStore((state) => state.isRunning)
  const timerMode = useTimerStore((state) => state.mode)
  const sessionLengthMinutes = useTimerStore((state) => state.sessionLengthMinutes)
  const elapsedSeconds = useTimerStore((state) => state.elapsedSeconds)
  const startTimer = useTimerStore((state) => state.startTimer)
  const pauseTimer = useTimerStore((state) => state.pauseTimer)
  const cancelTimer = useTimerStore((state) => state.cancelTimer)

  // Keyed by task id (like notesDraft below) rather than a plain useState, since this overlay
  // stays mounted across the whole queue - a bare useState would leak the previous task's manual
  // preset choice onto the next one instead of resetting to its own planned time.
  const [manualPreset, setManualPreset] = useState<{ taskId: number; minutes: number } | null>(null)
  const [customDraft, setCustomDraft] = useState<{ taskId: number; text: string } | null>(null)
  const [requestedTaskId, setRequestedTaskId] = useState<number | null>(null)
  const [celebrating, setCelebrating] = useState(false)
  const [panelCollapsed, setPanelCollapsed] = useState(false)
  const [addingSubtask, setAddingSubtask] = useState(false)
  const [subtaskTitle, setSubtaskTitle] = useState('')
  const [notesDraft, setNotesDraft] = useState<{ taskId: number; value: string } | null>(null)

  // The dialog stays mounted (open is just a prop), so the queue pointer must be re-seeded from
  // initialTaskId each time it opens - otherwise opening on task A, closing, then opening on task
  // B via its own "Focus" button would still land on wherever task A left the pointer. Tracked via
  // the react.dev "adjusting state when a prop changes" pattern (state, not a ref, so the
  // compiler-aware lint allows reading it during render).
  const [wasOpen, setWasOpen] = useState(open)
  if (open !== wasOpen) {
    setWasOpen(open)
    if (open && requestedTaskId !== initialTaskId) setRequestedTaskId(initialTaskId)
  }

  const openTasks = tasks.filter((t) => !t.completed)

  // The queue pointer falls back to the first open task whenever the requested one isn't
  // available (completed, deleted, or this being the first render) - computed directly during
  // render rather than synced back via an effect.
  const currentTaskId =
    requestedTaskId !== null && openTasks.some((t) => t.id === requestedTaskId)
      ? requestedTaskId
      : openTasks[0]?.id ?? null

  const currentIndex = openTasks.findIndex((t) => t.id === currentTaskId)
  const task = currentIndex >= 0 ? openTasks[currentIndex] : null

  // Defaults the session length to the card's own planned time, so a task estimated at 45m
  // starts a 45m focus session instead of always defaulting to the generic 25m preset.
  const defaultMinutes = task?.plannedMinutes && task.plannedMinutes > 0 ? task.plannedMinutes : 25
  const selectedPreset =
    manualPreset && task && manualPreset.taskId === task.id ? manualPreset.minutes : defaultMinutes
  const customMinutesInput = customDraft && task && customDraft.taskId === task.id ? customDraft.text : ''

  const isThisTask = task !== null && activeTaskId === task.id && timerMode === 'countdown'
  const hasSession = isThisTask && sessionLengthMinutes !== null
  const running = isRunning && isThisTask

  const totalTime = hasSession ? sessionLengthMinutes * 60 : selectedPreset * 60
  const timeLeft = hasSession ? Math.max(0, totalTime - elapsedSeconds) : totalTime

  const startPause = () => {
    if (!task) return
    if (running) {
      pauseTimer()
      return
    }
    startTimer(task.id, task.actualMinutes, {
      mode: 'countdown',
      sessionLengthMinutes: hasSession ? sessionLengthMinutes! : selectedPreset,
    })
  }

  const goTo = (delta: number) => {
    if (openTasks.length === 0) return
    const nextIndex = (currentIndex + delta + openTasks.length) % openTasks.length
    setRequestedTaskId(openTasks[nextIndex].id)
  }

  const markDone = () => {
    if (!task) return
    setCelebrating(true)
    onToggleComplete(task.id, true)
    setTimeout(() => {
      setCelebrating(false)
      // Falls back to the (now-updated) first open task once the completed one drops out.
      setRequestedTaskId(null)
    }, 650)
  }

  const handleAddSubtask = (event: FormEvent) => {
    event.preventDefault()
    if (!task || !subtaskTitle.trim()) return
    onAddSubtask?.(task.id, subtaskTitle)
    setSubtaskTitle('')
    setAddingSubtask(false)
  }

  // Notes are edited locally and flushed to the parent on a short debounce, rather than on every
  // keystroke, so a fast typist doesn't trigger a save request per character. The draft is keyed
  // by task id so it's only shown while that same task is on screen - no effect needed to reset
  // it when the focused task changes, since a stale draft simply stops matching.
  const notesSaveTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const handleNotesChange = (value: string) => {
    if (!task) return
    setNotesDraft({ taskId: task.id, value })
    if (notesSaveTimeoutRef.current) clearTimeout(notesSaveTimeoutRef.current)
    notesSaveTimeoutRef.current = setTimeout(() => onUpdateNotes?.(task.id, value), NOTES_SAVE_DEBOUNCE_MS)
  }
  useEffect(() => {
    return () => {
      if (notesSaveTimeoutRef.current) clearTimeout(notesSaveTimeoutRef.current)
    }
  }, [])
  const notesValue = notesDraft && notesDraft.taskId === task?.id ? notesDraft.value : task?.description ?? ''

  // Latest callback lives in a ref so the keydown listener can stay bound for the whole time the
  // overlay is open instead of being torn down and rebound every time the timer ticks.
  const startPauseRef = useRef(startPause)
  useEffect(() => {
    startPauseRef.current = startPause
  })
  const goToRef = useRef(goTo)
  useEffect(() => {
    goToRef.current = goTo
  })

  useEffect(() => {
    if (!open) return
    const handler = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement | null
      if (target && ['INPUT', 'TEXTAREA'].includes(target.tagName)) return
      if (event.code === 'Space') {
        event.preventDefault()
        startPauseRef.current()
      } else if (event.key === 'j' || event.key === 'ArrowDown') {
        event.preventDefault()
        goToRef.current(1)
      } else if (event.key === 'k' || event.key === 'ArrowUp') {
        event.preventDefault()
        goToRef.current(-1)
      }
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [open])

  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-background data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:animate-in data-[state=open]:fade-in-0" />
        <DialogPrimitive.Content
          className="fixed inset-0 z-50 flex outline-none data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 data-[state=open]:animate-in data-[state=open]:fade-in-0 data-[state=open]:zoom-in-95"
          onEscapeKeyDown={() => onOpenChange(false)}
        >
          <DialogPrimitive.Title className="sr-only">Execution mode{task ? `: ${task.title || 'this task'}` : ''}</DialogPrimitive.Title>

          {/* Left panel - today's queue, muted relative to center stage, checking off completed tasks. */}
          <aside
            className={cn(
              'flex h-full shrink-0 flex-col overflow-hidden border-r border-border bg-card/60 transition-[width] duration-200',
              panelCollapsed ? 'w-0' : 'w-72'
            )}
          >
            <div className="flex items-center justify-between gap-2 px-4 pt-5">
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Today&apos;s queue</span>
            </div>
            <div className="mt-2 flex-1 overflow-y-auto px-2 pb-4">
              {tasks.length === 0 ? (
                <p className="px-2 py-4 text-xs text-muted-foreground">Nothing scheduled today.</p>
              ) : (
                <ul className="space-y-0.5">
                  {tasks.map((t) => {
                    const isActive = !t.completed && t.id === currentTaskId
                    return (
                      <li key={t.id}>
                        <button
                          type="button"
                          onClick={() => !t.completed && setRequestedTaskId(t.id)}
                          disabled={t.completed}
                          className={cn(
                            'flex w-full items-center gap-2 rounded-md px-2.5 py-2 text-left text-sm transition-colors',
                            isActive
                              ? 'bg-primary/15 text-foreground'
                              : t.completed
                                ? 'cursor-default text-muted-foreground/60'
                                : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground'
                          )}
                        >
                          {t.completed ? (
                            <CheckCircle2 className="h-4 w-4 shrink-0 text-status-done" />
                          ) : (
                            <Circle className={cn('h-4 w-4 shrink-0', isActive ? 'text-primary' : 'text-muted-foreground/50')} />
                          )}
                          <span className={cn('truncate', t.completed && 'line-through')}>{t.title || 'Untitled task'}</span>
                        </button>
                      </li>
                    )
                  })}
                </ul>
              )}
            </div>
          </aside>

          <div className="relative flex min-w-0 flex-1 flex-col overflow-y-auto bg-background">
            <button
              type="button"
              onClick={() => setPanelCollapsed((c) => !c)}
              className="absolute left-4 top-6 z-10 rounded-full p-2 text-muted-foreground opacity-70 transition-opacity hover:bg-accent hover:opacity-100 focus-visible:opacity-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              aria-label={panelCollapsed ? 'Show today\'s queue' : 'Hide today\'s queue'}
            >
              {panelCollapsed ? <PanelLeftOpen className="h-5 w-5" /> : <PanelLeftClose className="h-5 w-5" />}
            </button>

            <DialogPrimitive.Close
              className="absolute right-6 top-6 z-10 rounded-full p-2.5 text-muted-foreground opacity-70 transition-opacity hover:bg-accent hover:opacity-100 focus-visible:opacity-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              aria-label="Exit execution mode"
            >
              <X className="h-5 w-5" />
            </DialogPrimitive.Close>

            {task ? (
              <div className="mx-auto flex w-full max-w-4xl flex-1 items-start gap-10 px-8 py-16 sm:px-12">
                {/* Left/center: title, editable subtasks, editable notes - typography first, no chrome. */}
                <div className="min-w-0 flex-1 space-y-6">
                  <div className="space-y-2">
                    {task.category && (
                      <span className={cn('inline-block rounded-full px-2 py-0.5 text-[11px] font-medium', channelPillClass(task.category))}>
                        #{task.category}
                      </span>
                    )}
                    <h1 className="text-2xl font-semibold leading-tight text-foreground">{task.title || 'Untitled task'}</h1>
                  </div>

                  <div className="space-y-1.5">
                    <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Subtasks</span>
                    <ul className="flex flex-col gap-1">
                      {task.subtasks.map((subtask) => (
                        <li key={subtask.id}>
                          <button
                            type="button"
                            onClick={() => onToggleSubtask?.(task.id, subtask.id, !subtask.completed)}
                            disabled={!onToggleSubtask}
                            className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-sm transition-colors hover:bg-accent/50 disabled:hover:bg-transparent"
                          >
                            {subtask.completed ? (
                              <CheckCircle2 className="h-4 w-4 shrink-0 text-status-done" />
                            ) : (
                              <Circle className="h-4 w-4 shrink-0 text-muted-foreground/50" />
                            )}
                            <span className={cn('truncate text-foreground', subtask.completed && 'text-muted-foreground line-through')}>
                              {subtask.title}
                            </span>
                          </button>
                        </li>
                      ))}
                      {onAddSubtask &&
                        (addingSubtask ? (
                          <form onSubmit={handleAddSubtask}>
                            <input
                              autoFocus
                              value={subtaskTitle}
                              onChange={(event) => setSubtaskTitle(event.target.value)}
                              onBlur={() => {
                                if (subtaskTitle.trim()) onAddSubtask(task.id, subtaskTitle)
                                setSubtaskTitle('')
                                setAddingSubtask(false)
                              }}
                              onKeyDown={(event) => {
                                if (event.key === 'Escape') {
                                  setSubtaskTitle('')
                                  setAddingSubtask(false)
                                }
                              }}
                              placeholder="Subtask title"
                              className="ml-6 h-7 w-full max-w-xs rounded-md border-none bg-muted/50 px-2 text-sm text-foreground shadow-none outline-none focus-visible:ring-1 focus-visible:ring-ring"
                            />
                          </form>
                        ) : (
                          <button
                            type="button"
                            onClick={() => setAddingSubtask(true)}
                            className="flex items-center gap-1.5 self-start rounded-md px-2 py-1.5 text-xs text-muted-foreground hover:bg-accent/50 hover:text-foreground"
                          >
                            <Plus className="h-3.5 w-3.5" />
                            Add subtask
                          </button>
                        ))}
                    </ul>
                  </div>

                  <div className="space-y-1.5">
                    <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Notes</span>
                    <Textarea
                      value={notesValue}
                      onChange={(event) => handleNotesChange(event.target.value)}
                      disabled={!onUpdateNotes}
                      placeholder="Jot down notes while you work..."
                      className="min-h-28 resize-none border-none bg-muted/40 text-sm text-foreground shadow-none focus-visible:ring-1"
                    />
                  </div>

                  <button
                    type="button"
                    onClick={markDone}
                    className="flex items-center gap-2 rounded-full bg-status-done px-5 py-2 text-sm font-semibold text-background transition-opacity hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
                  >
                    <CheckCircle2 className="h-4 w-4" />
                    Mark done
                  </button>
                </div>

                {/* Top right: minimalist digital timer + controls - no gauges, no clutter. */}
                <div className="relative flex w-52 shrink-0 flex-col items-center gap-4 rounded-lg border border-border bg-card/50 p-4">
                  {celebrating && (
                    <div className="pointer-events-none absolute inset-0 z-10 flex items-center justify-center">
                      <span className="absolute h-16 w-16 animate-ping rounded-full bg-status-done/30" />
                      <CheckCircle2 className="relative h-10 w-10 text-status-done duration-300 animate-in zoom-in-50 fade-in" />
                    </div>
                  )}

                  <span
                    className="font-mono text-3xl font-semibold tabular-nums text-foreground"
                    role="timer"
                    aria-label={`Timer: ${formatDigitalTime(timeLeft)} remaining`}
                  >
                    {formatDigitalTime(timeLeft)}
                  </span>

                  {!hasSession && (
                    <div className="flex items-center gap-1 rounded-full bg-muted/60 p-1" role="group" aria-label="Session length">
                      {PRESETS_MINUTES.map((minutes) => (
                        <button
                          key={minutes}
                          type="button"
                          onClick={() => {
                            if (!task) return
                            setManualPreset({ taskId: task.id, minutes })
                            setCustomDraft(null)
                          }}
                          className={cn(
                            'rounded-full px-2.5 py-1 font-mono text-xs tabular-nums transition-colors',
                            selectedPreset === minutes && customMinutesInput === ''
                              ? 'bg-primary text-primary-foreground'
                              : 'text-muted-foreground hover:text-foreground'
                          )}
                        >
                          {minutes}m
                        </button>
                      ))}
                      <input
                        type="text"
                        inputMode="numeric"
                        value={customMinutesInput}
                        onChange={(event) => {
                          if (!task) return
                          const digitsOnly = event.target.value.replace(/\D/g, '').slice(0, 3)
                          setCustomDraft({ taskId: task.id, text: digitsOnly })
                          const parsed = Number(digitsOnly)
                          if (parsed > 0) setManualPreset({ taskId: task.id, minutes: Math.min(parsed, 300) })
                        }}
                        placeholder="custom"
                        aria-label="Custom session length in minutes"
                        className={cn(
                          'w-14 rounded-full bg-transparent px-2 py-1 text-center font-mono text-xs tabular-nums outline-none placeholder:text-muted-foreground/60',
                          customMinutesInput !== '' ? 'bg-primary text-primary-foreground' : 'text-muted-foreground'
                        )}
                      />
                    </div>
                  )}

                  <button
                    type="button"
                    onClick={startPause}
                    className="flex w-full items-center justify-center gap-2 rounded-full bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
                  >
                    {running ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
                    {running ? 'Pause' : hasSession ? 'Resume' : 'Start'}
                  </button>

                  <div className="flex w-full items-center gap-2">
                    {/* "Break" steps away from the session the same way Pause does - the store has
                     * no separate break-timer state, so this simply surfaces pausing under the
                     * label ADHD users look for when they need a breather. */}
                    <button
                      type="button"
                      onClick={pauseTimer}
                      disabled={!running}
                      className="flex flex-1 items-center justify-center gap-1.5 rounded-full border border-border px-3 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:border-primary/40 hover:text-foreground disabled:pointer-events-none disabled:opacity-40"
                    >
                      <Pause className="h-3.5 w-3.5" />
                      Break
                    </button>
                    <button
                      type="button"
                      onClick={() => cancelTimer()}
                      disabled={!hasSession}
                      className="flex flex-1 items-center justify-center gap-1.5 rounded-full border border-border px-3 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:border-destructive/40 hover:text-destructive disabled:pointer-events-none disabled:opacity-40"
                    >
                      <Square className="h-3.5 w-3.5" />
                      Cancel
                    </button>
                  </div>

                  <span className="text-center text-[10px] text-muted-foreground">
                    <kbd className="rounded border border-border bg-muted px-1 py-0.5 font-mono">Space</kbd> start/pause ·{' '}
                    <kbd className="rounded border border-border bg-muted px-1 py-0.5 font-mono">J</kbd>/
                    <kbd className="rounded border border-border bg-muted px-1 py-0.5 font-mono">K</kbd> switch
                  </span>
                </div>
              </div>
            ) : (
              <div className="flex flex-1 flex-col items-center justify-center gap-3 text-center">
                <CheckCircle2 className="h-12 w-12 text-status-done" />
                <h2 className="text-2xl font-semibold text-foreground">All done for today</h2>
                <p className="text-sm text-muted-foreground">Nothing left in the queue. Nice work.</p>
              </div>
            )}
          </div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  )
}
