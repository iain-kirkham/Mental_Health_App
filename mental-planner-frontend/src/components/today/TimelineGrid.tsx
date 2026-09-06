'use client'

import { useDraggable, useDroppable } from '@dnd-kit/core'
import { CSS } from '@dnd-kit/utilities'
import { format, isToday } from 'date-fns'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { channelBorderClass } from '@/lib/channel-color'
import { makeGridId, makeResizeId } from '@/lib/timeline-drag-ids'
import type { TaskResponseDTO } from '@/types'

export const GRID_START_HOUR = 6
export const GRID_END_HOUR = 22
export const GRID_HOURS = GRID_END_HOUR - GRID_START_HOUR
export const HOUR_HEIGHT = 64
export const TIMELINE_DROPPABLE_ID = 'timeline-grid'

function formatHourLabel(hour: number) {
  const h = ((hour + 11) % 12) + 1
  return `${h} ${hour < 12 ? 'AM' : 'PM'}`
}

function minutesFromGridStart(instant: Date) {
  return (instant.getHours() - GRID_START_HOUR) * 60 + instant.getMinutes()
}

interface TimelineGridProps {
  date: Date
  tasks: TaskResponseDTO[]
  onOpenDetail: (task: TaskResponseDTO) => void
  onUnschedule?: (taskId: number) => void
}

interface TimelineBlockProps {
  task: TaskResponseDTO
  top: number
  height: number
  onOpenDetail: (task: TaskResponseDTO) => void
  onUnschedule?: (taskId: number) => void
}

function TimelineBlock({ task, top, height, onOpenDetail, onUnschedule }: TimelineBlockProps) {
  const start = new Date(task.startTime!)
  const end = new Date(task.endTime!)
  const {
    attributes: moveAttributes,
    listeners: moveListeners,
    setNodeRef: setMoveNodeRef,
    transform: moveTransform,
    isDragging: moveIsDragging,
  } = useDraggable({ id: makeGridId(task.id) })
  const {
    attributes: resizeAttributes,
    listeners: resizeListeners,
    setNodeRef: setResizeNodeRef,
    transform: resizeTransform,
    isDragging: resizeIsDragging,
  } = useDraggable({ id: makeResizeId(task.id) })

  // The resize handle is a sibling draggable, not nested inside the move draggable's own node -
  // dnd-kit doesn't support one draggable inside another, so they share the block visually but
  // never share a DOM ancestor/descendant relationship. Only the handle actively being dragged
  // gets a non-null transform, so this is a no-op for every block except the one being resized.
  const liveHeight = Math.max(22, height + (resizeTransform?.y ?? 0))

  return (
    <div
      className="group absolute left-0 right-0"
      style={{
        top,
        height: liveHeight,
        transform: moveTransform ? CSS.Translate.toString(moveTransform) : undefined,
      }}
    >
      <div
        ref={setMoveNodeRef}
        {...moveAttributes}
        {...moveListeners}
        className={cn(
          'h-full w-full touch-none cursor-grab overflow-hidden rounded-md bg-card px-2 py-1 text-left shadow-sm transition-shadow hover:shadow-md active:cursor-grabbing',
          channelBorderClass(task.category ?? 'default'),
          task.completed && 'opacity-50',
          moveIsDragging && 'z-20 opacity-60 shadow-lg'
        )}
      >
        <button type="button" onClick={() => onOpenDetail(task)} className="block w-full text-left">
          <span
            className={cn('block truncate text-xs font-medium leading-tight text-foreground', task.completed && 'line-through')}
          >
            {task.title || 'Untitled task'}
          </span>
          <span className="block truncate text-[10px] text-muted-foreground">
            {format(start, 'h:mm a')} – {format(end, 'h:mm a')}
          </span>
        </button>
        {onUnschedule && (
          <button
            type="button"
            onPointerDown={(event) => event.stopPropagation()}
            onClick={(event) => {
              event.stopPropagation()
              onUnschedule(task.id)
            }}
            className="absolute right-1 top-1 rounded-sm p-0.5 text-muted-foreground opacity-0 hover:bg-muted hover:text-foreground group-hover:opacity-100"
            aria-label="Remove from schedule"
          >
            <X className="h-3 w-3" />
          </button>
        )}
      </div>

      <div
        ref={setResizeNodeRef}
        {...resizeAttributes}
        {...resizeListeners}
        className={cn(
          'absolute inset-x-0 bottom-0 z-10 h-2 touch-none cursor-ns-resize rounded-b-md opacity-0 transition-opacity hover:bg-primary/60 group-hover:opacity-100',
          resizeIsDragging && 'bg-primary/60 opacity-100'
        )}
        aria-label={`Resize ${task.title || 'task'}`}
      />
    </div>
  )
}

export default function TimelineGrid({ date, tasks, onOpenDetail, onUnschedule }: TimelineGridProps) {
  const scheduled = tasks.filter((task) => task.startTime && task.endTime)
  const gridHeight = GRID_HOURS * HOUR_HEIGHT
  const { setNodeRef, isOver } = useDroppable({ id: TIMELINE_DROPPABLE_ID })

  const now = new Date()
  const nowOffset = isToday(date) ? minutesFromGridStart(now) : null
  const showNowLine = nowOffset !== null && nowOffset >= 0 && nowOffset <= GRID_HOURS * 60

  return (
    <div className="h-full min-w-0 flex-1 overflow-y-auto rounded-md border border-border bg-card/40">
      <div
        ref={setNodeRef}
        className={cn('relative ml-14 mr-3 transition-colors', isOver && 'bg-primary/5')}
        style={{ height: gridHeight }}
      >
        {Array.from({ length: GRID_HOURS + 1 }, (_, i) => GRID_START_HOUR + i).map((hour, i) => (
          <div key={hour} className="absolute inset-x-0 border-t border-border/50" style={{ top: i * HOUR_HEIGHT }}>
            <span className="absolute -left-14 -top-2.5 w-12 pr-2 text-right font-mono text-[10px] text-muted-foreground">
              {formatHourLabel(hour)}
            </span>
          </div>
        ))}

        {showNowLine && (
          <div
            className="absolute inset-x-0 z-10 flex items-center gap-1"
            style={{ top: (nowOffset! / 60) * HOUR_HEIGHT }}
          >
            <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-destructive" />
            <span className="h-px flex-1 bg-destructive/70" />
          </div>
        )}

        {scheduled.map((task) => {
          const start = new Date(task.startTime!)
          const end = new Date(task.endTime!)
          const startMin = Math.max(0, minutesFromGridStart(start))
          const endMin = Math.min(GRID_HOURS * 60, minutesFromGridStart(end))
          if (endMin <= startMin) return null
          const top = (startMin / 60) * HOUR_HEIGHT
          const height = Math.max(((endMin - startMin) / 60) * HOUR_HEIGHT, 22)

          return (
            <TimelineBlock
              key={task.id}
              task={task}
              top={top}
              height={height}
              onOpenDetail={onOpenDetail}
              onUnschedule={onUnschedule}
            />
          )
        })}
      </div>
    </div>
  )
}
