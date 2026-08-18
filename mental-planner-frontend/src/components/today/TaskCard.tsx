'use client'

import { useState, type FormEvent } from 'react'
import type { DraggableAttributes, DraggableSyntheticListeners } from '@dnd-kit/core'
import { Plus } from 'lucide-react'
import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import { priorityBorderClass } from '@/lib/task-priority'
import { useTimerStore } from '@/store/timerStore'
import type { TaskResponseDTO } from '@/types'

interface TaskCardProps {
  task: TaskResponseDTO
  onToggleComplete: (id: number, completed: boolean) => void
  onToggleSubtask: (taskId: number, subtaskId: number, completed: boolean) => void
  onAddSubtask: (taskId: number, title: string) => void
  onOpenDetail: (task: TaskResponseDTO) => void
  dragHandleAttributes?: DraggableAttributes
  dragHandleListeners?: DraggableSyntheticListeners
  isOverlay?: boolean
}

function formatLiveTime(totalSeconds: number) {
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
}

function formatStartTime(instant: string) {
  return new Date(instant).toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' })
}

const CHECKED_GREEN = 'data-[state=checked]:bg-chart-2 data-[state=checked]:border-chart-2'

export default function TaskCard({
  task,
  onToggleComplete,
  onToggleSubtask,
  onAddSubtask,
  onOpenDetail,
  dragHandleAttributes,
  dragHandleListeners,
  isOverlay,
}: TaskCardProps) {
  const [addingSubtask, setAddingSubtask] = useState(false)
  const [subtaskTitle, setSubtaskTitle] = useState('')

  const activeTaskId = useTimerStore((state) => state.activeTaskId)
  const isRunning = useTimerStore((state) => state.isRunning)
  const elapsedSeconds = useTimerStore((state) => state.elapsedSeconds)

  const isTimerRunning = isRunning && activeTaskId === task.id
  const liveSeconds = isTimerRunning ? task.actualMinutes * 60 + elapsedSeconds : null

  const hasSubtasks = task.subtasks.length > 0
  const hasTimeBadge = isTimerRunning || task.plannedMinutes != null || task.actualMinutes > 0
  const hasPlanned = task.plannedMinutes != null && task.plannedMinutes > 0
  // Always render Actual as minutes:seconds (not hours:minutes) so restarting the timer
  // doesn't visually jump - e.g. a paused 45m task must read "45:00", not "0:45", the
  // instant you press play, since the live ticker uses the same minutes:seconds format.
  const actualDisplay = formatLiveTime(isTimerRunning && liveSeconds != null ? liveSeconds : task.actualMinutes * 60)
  const timeBadgeText = hasPlanned ? `${actualDisplay} / ${formatLiveTime((task.plannedMinutes ?? 0) * 60)}` : actualDisplay

  const handleAddSubtask = (event: FormEvent) => {
    event.preventDefault()
    event.stopPropagation()
    if (!subtaskTitle.trim()) return
    onAddSubtask(task.id, subtaskTitle)
    setSubtaskTitle('')
  }

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => onOpenDetail(task)}
      onKeyDown={(event) => {
        if (event.key === 'Enter') onOpenDetail(task)
      }}
      className={cn(
        'group flex cursor-pointer flex-col gap-1 rounded-md border border-border bg-card p-2.5 shadow-sm transition-all duration-300 ease-in-out hover:shadow-md',
        priorityBorderClass(task.priority),
        task.completed ? 'opacity-60' : 'opacity-100',
        isOverlay ? 'shadow-lg' : ''
      )}
      {...dragHandleAttributes}
      {...dragHandleListeners}
    >
      {/* Top: start time + title + time badge */}
      <div className="flex items-start gap-2">
        <div className="flex flex-1 items-baseline gap-1.5 min-w-0">
          {task.startTime && (
            <span className="shrink-0 font-mono text-[10px] text-muted-foreground">{formatStartTime(task.startTime)}</span>
          )}
          <span
            className={cn(
              'flex-1 truncate text-sm leading-snug transition-colors duration-300 ease-in-out',
              task.completed ? 'text-muted-foreground line-through' : 'text-foreground'
            )}
          >
            {task.title || 'Untitled task'}
          </span>
        </div>
        {hasTimeBadge && (
          <span
            className={cn(
              'shrink-0 rounded-sm px-1.5 py-0.5 font-mono text-[10px] font-semibold tabular-nums',
              isTimerRunning ? 'bg-chart-2/20 text-chart-2' : 'bg-primary/15 text-primary'
            )}
          >
            {timeBadgeText}
          </span>
        )}
      </div>

      {/* Middle: inline subtasks */}
      {(hasSubtasks || addingSubtask) && (
        <div className="ml-0.5 flex flex-col gap-1.5">
          {task.subtasks.map((subtask) => (
            <div
              key={subtask.id}
              className={cn(
                'flex items-center gap-1.5 transition-opacity duration-300 ease-in-out',
                subtask.completed ? 'opacity-60' : 'opacity-100'
              )}
            >
              <Checkbox
                checked={subtask.completed}
                onCheckedChange={(checked) => onToggleSubtask(task.id, subtask.id, checked === true)}
                onPointerDown={(event) => event.stopPropagation()}
                onClick={(event) => event.stopPropagation()}
                className={cn('h-3.5 w-3.5 rounded-full', CHECKED_GREEN)}
              />
              <span
                className={cn(
                  'flex-1 truncate text-xs leading-snug transition-colors duration-300 ease-in-out',
                  subtask.completed ? 'text-muted-foreground/70 line-through' : 'text-muted-foreground'
                )}
              >
                {subtask.title}
              </span>
            </div>
          ))}

          {addingSubtask ? (
            <form onSubmit={handleAddSubtask} onClick={(event) => event.stopPropagation()}>
              <Input
                autoFocus
                value={subtaskTitle}
                onChange={(event) => setSubtaskTitle(event.target.value)}
                onPointerDown={(event) => event.stopPropagation()}
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
                className="h-6 border-none bg-muted/50 px-1.5 text-xs shadow-none focus-visible:ring-1"
              />
            </form>
          ) : (
            <button
              type="button"
              onPointerDown={(event) => event.stopPropagation()}
              onClick={(event) => {
                event.stopPropagation()
                setAddingSubtask(true)
              }}
              className="flex items-center gap-1 self-start rounded-sm px-1 py-0.5 text-[11px] text-muted-foreground opacity-0 hover:bg-muted/50 group-hover:opacity-100"
            >
              <Plus className="h-3 w-3" />
              subtask
            </button>
          )}
        </div>
      )}

      {/* Bottom: complete checkbox + category */}
      <div className="flex items-center gap-2 pt-1">
        <Checkbox
          checked={task.completed}
          onCheckedChange={(checked) => onToggleComplete(task.id, checked === true)}
          onPointerDown={(event) => event.stopPropagation()}
          onClick={(event) => event.stopPropagation()}
          className={cn('rounded-full', CHECKED_GREEN)}
        />
        <div className="flex-1" />
        {task.category && (
          <span className="rounded-sm bg-accent px-1.5 py-0.5 text-[11px] font-medium text-accent-foreground">
            #{task.category}
          </span>
        )}
      </div>
    </div>
  )
}
