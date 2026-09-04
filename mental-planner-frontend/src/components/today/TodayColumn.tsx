'use client'

import { format } from 'date-fns'
import { Plus } from 'lucide-react'
import TaskCard from './TaskCard'
import { Button } from '@/components/ui/button'
import { useTimerStore } from '@/store/timerStore'
import type { TaskResponseDTO } from '@/types'

interface TodayColumnProps {
  date: Date
  tasks: TaskResponseDTO[]
  isLoading?: boolean
  onRequestAddTask: () => void
  onToggleComplete: (id: number, completed: boolean) => void
  onToggleSubtask: (taskId: number, subtaskId: number, completed: boolean) => void
  onAddSubtask: (taskId: number, title: string) => void
  onOpenDetail: (task: TaskResponseDTO) => void
  onOpenFocus: (task: TaskResponseDTO) => void
}

/** "1:34" style hours:minutes, matching Sunsama's planned/actual readout. */
function formatHoursMinutes(totalMinutes: number) {
  const hours = Math.floor(totalMinutes / 60)
  const minutes = Math.round(totalMinutes % 60)
  return `${hours}:${String(minutes).padStart(2, '0')}`
}

export default function TodayColumn({
  date,
  tasks,
  isLoading,
  onRequestAddTask,
  onToggleComplete,
  onToggleSubtask,
  onAddSubtask,
  onOpenDetail,
  onOpenFocus,
}: TodayColumnProps) {
  const activeTaskId = useTimerStore((state) => state.activeTaskId)
  const isRunning = useTimerStore((state) => state.isRunning)
  const elapsedSeconds = useTimerStore((state) => state.elapsedSeconds)

  const plannedTotal = tasks.reduce((sum, task) => sum + (task.plannedMinutes ?? 0), 0)
  const liveExtraMinutes =
    isRunning && activeTaskId !== null && tasks.some((task) => task.id === activeTaskId) ? elapsedSeconds / 60 : 0
  const actualTotal = tasks.reduce((sum, task) => sum + task.actualMinutes, 0) + liveExtraMinutes
  const progressPct = plannedTotal > 0 ? Math.min(100, (actualTotal / plannedTotal) * 100) : 0

  return (
    <div className="flex h-full min-w-0 flex-col">
      <div className="flex items-baseline justify-between gap-2 px-1">
        <div>
          <h2 className="text-lg font-semibold text-foreground">{format(date, 'EEEE')}</h2>
          <p className="text-xs text-muted-foreground">{format(date, 'MMMM d')}</p>
        </div>
        {plannedTotal > 0 && (
          <span className="font-mono text-sm tabular-nums text-muted-foreground">
            {formatHoursMinutes(actualTotal)} / {formatHoursMinutes(plannedTotal)}
          </span>
        )}
      </div>

      {plannedTotal > 0 && (
        <div className="mt-3 h-1.5 w-full overflow-hidden rounded-full bg-muted px-1">
          <div
            className="h-full rounded-full bg-primary transition-all duration-300 ease-in-out"
            style={{ width: `${progressPct}%` }}
          />
        </div>
      )}

      <div className="px-1 pb-2 pt-3">
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="h-7 w-full justify-start px-2 text-xs text-muted-foreground hover:text-foreground"
          onClick={onRequestAddTask}
          disabled={isLoading}
        >
          <Plus className="mr-1 h-3.5 w-3.5" />
          Add task
        </Button>
      </div>

      <div className="flex-1 space-y-2 overflow-y-auto px-1 pb-3">
        {isLoading ? (
          <div className="space-y-2" aria-hidden>
            <div className="h-14 animate-pulse rounded-md bg-muted/60" />
            <div className="h-14 animate-pulse rounded-md bg-muted/40" />
          </div>
        ) : tasks.length === 0 ? (
          <p className="px-1 py-4 text-xs text-muted-foreground">Nothing scheduled for today.</p>
        ) : (
          tasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              onToggleComplete={onToggleComplete}
              onToggleSubtask={onToggleSubtask}
              onAddSubtask={onAddSubtask}
              onOpenDetail={onOpenDetail}
              onOpenFocus={onOpenFocus}
            />
          ))
        )}
      </div>
    </div>
  )
}
