'use client'

import { useDroppable } from '@dnd-kit/core'
import { SortableContext, useSortable, verticalListSortingStrategy } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { format, isToday } from 'date-fns'
import { Plus } from 'lucide-react'
import DraggableTaskCardShell from './DraggableTaskCardShell'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import type { TaskResponseDTO } from '@/types'

interface DayColumnProps {
  dateKey: string
  date: Date
  tasks: TaskResponseDTO[]
  isLoading?: boolean
  onRequestAddTask: (dateKey: string) => void
  onToggleComplete: (id: number, completed: boolean) => void
  onToggleSubtask: (taskId: number, subtaskId: number, completed: boolean) => void
  onAddSubtask: (taskId: number, title: string) => void
  onOpenDetail: (task: TaskResponseDTO) => void
  onOpenFocus: (task: TaskResponseDTO) => void
}

interface SortableTaskCardProps {
  task: TaskResponseDTO
  onToggleComplete: (id: number, completed: boolean) => void
  onToggleSubtask: (taskId: number, subtaskId: number, completed: boolean) => void
  onAddSubtask: (taskId: number, title: string) => void
  onOpenDetail: (task: TaskResponseDTO) => void
  onOpenFocus: (task: TaskResponseDTO) => void
  dateKey: string
}

function SortableTaskCard({ task, dateKey, ...handlers }: SortableTaskCardProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: String(task.id),
    data: { dateKey },
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
  }

  return (
    <DraggableTaskCardShell
      task={task}
      setNodeRef={setNodeRef}
      attributes={attributes}
      listeners={listeners}
      style={style}
      {...handlers}
    />
  )
}

const WORKLOAD_WARN_MINUTES = 8 * 60

function formatWorkload(minutes: number) {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) return `${mins}m`
  if (mins === 0) return `${hours}h`
  return `${hours}h ${mins}m`
}

export default function DayColumn({
  dateKey,
  date,
  tasks,
  isLoading,
  onRequestAddTask,
  onToggleComplete,
  onToggleSubtask,
  onAddSubtask,
  onOpenDetail,
  onOpenFocus,
}: DayColumnProps) {
  const { setNodeRef, isOver } = useDroppable({ id: dateKey })
  const today = isToday(date)

  const workloadMinutes = tasks.reduce((sum, task) => sum + (task.plannedMinutes ?? 0), 0)
  const overloaded = workloadMinutes > WORKLOAD_WARN_MINUTES

  return (
    <div className="flex h-full min-w-0 flex-col border-r border-black/5 last:border-r-0 dark:border-white/5">
      <div className="flex items-center justify-between gap-2 px-3 pt-3">
        <div className="flex items-center gap-1.5">
          {today && <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-primary" aria-hidden />}
          <h2 className="text-xs font-medium text-foreground/80">{format(date, 'EEE')}</h2>
          <span className="text-xs font-normal text-muted-foreground">{format(date, 'MMM d')}</span>
        </div>
        {workloadMinutes > 0 && (
          <span
            className={cn(
              'shrink-0 rounded-sm px-2 py-0.5 font-mono text-[11px] font-medium tabular-nums',
              overloaded ? 'bg-destructive/15 text-destructive' : 'text-muted-foreground/70'
            )}
          >
            {formatWorkload(workloadMinutes)}
          </span>
        )}
      </div>

      <div className="px-3 pb-2 pt-2">
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="h-7 w-full justify-start px-2 text-xs text-muted-foreground hover:text-foreground"
          onClick={() => onRequestAddTask(dateKey)}
          disabled={isLoading}
        >
          <Plus className="mr-1 h-3.5 w-3.5" />
          Add task
        </Button>
      </div>

      <div
        ref={setNodeRef}
        className={cn('flex-1 space-y-2 overflow-y-auto px-3 pb-3', isOver ? 'bg-accent/20' : '')}
      >
        {isLoading ? (
          <div className="space-y-2" aria-hidden>
            <div className="h-14 animate-pulse rounded-md bg-muted/60" />
            <div className="h-14 animate-pulse rounded-md bg-muted/40" />
          </div>
        ) : (
          <SortableContext items={tasks.map((task) => String(task.id))} strategy={verticalListSortingStrategy}>
            {tasks.map((task) => (
              <SortableTaskCard
                key={task.id}
                task={task}
                dateKey={dateKey}
                onToggleComplete={onToggleComplete}
                onToggleSubtask={onToggleSubtask}
                onAddSubtask={onAddSubtask}
                onOpenDetail={onOpenDetail}
                onOpenFocus={onOpenFocus}
              />
            ))}
          </SortableContext>
        )}
      </div>
    </div>
  )
}
