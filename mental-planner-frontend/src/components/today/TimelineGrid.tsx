'use client'

import { format, isToday } from 'date-fns'
import { cn } from '@/lib/utils'
import { channelBorderClass } from '@/lib/channel-color'
import type { TaskResponseDTO } from '@/types'

const GRID_START_HOUR = 6
const GRID_END_HOUR = 22
const GRID_HOURS = GRID_END_HOUR - GRID_START_HOUR
const HOUR_HEIGHT = 64

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
}

export default function TimelineGrid({ date, tasks, onOpenDetail }: TimelineGridProps) {
  const scheduled = tasks.filter((task) => task.startTime && task.endTime)
  const gridHeight = GRID_HOURS * HOUR_HEIGHT

  const now = new Date()
  const nowOffset = isToday(date) ? minutesFromGridStart(now) : null
  const showNowLine = nowOffset !== null && nowOffset >= 0 && nowOffset <= GRID_HOURS * 60

  return (
    <div className="h-full min-w-0 flex-1 overflow-y-auto rounded-md border border-border bg-card/40">
      <div className="relative ml-14 mr-3" style={{ height: gridHeight }}>
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
            <button
              key={task.id}
              type="button"
              onClick={() => onOpenDetail(task)}
              className={cn(
                'absolute left-0 right-0 overflow-hidden rounded-md bg-card px-2 py-1 text-left shadow-sm transition-shadow hover:shadow-md',
                channelBorderClass(task.category ?? 'default'),
                task.completed && 'opacity-50'
              )}
              style={{ top, height }}
            >
              <span
                className={cn(
                  'block truncate text-xs font-medium leading-tight text-foreground',
                  task.completed && 'line-through'
                )}
              >
                {task.title || 'Untitled task'}
              </span>
              <span className="block truncate text-[10px] text-muted-foreground">
                {format(start, 'h:mm a')} – {format(end, 'h:mm a')}
              </span>
            </button>
          )
        })}
      </div>
    </div>
  )
}
