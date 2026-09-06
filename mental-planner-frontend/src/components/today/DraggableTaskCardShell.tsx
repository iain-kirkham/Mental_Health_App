'use client'

import type { CSSProperties } from 'react'
import type { DraggableAttributes, DraggableSyntheticListeners } from '@dnd-kit/core'
import TaskCard from './TaskCard'
import type { TaskResponseDTO } from '@/types'

interface DraggableTaskCardShellProps {
  task: TaskResponseDTO
  setNodeRef: (node: HTMLElement | null) => void
  attributes: DraggableAttributes
  listeners: DraggableSyntheticListeners
  style?: CSSProperties
  className?: string
  onToggleComplete: (id: number, completed: boolean) => void
  onToggleSubtask: (taskId: number, subtaskId: number, completed: boolean) => void
  onAddSubtask: (taskId: number, title: string) => void
  onOpenDetail: (task: TaskResponseDTO) => void
  onOpenFocus: (task: TaskResponseDTO) => void
}

/** Shared shell for the two dnd-kit-backed TaskCard wrappers (DayColumn's sortable card within a
 * day column, TodayColumn's plain draggable queue card). Each caller calls its own dnd-kit hook
 * (useSortable vs useDraggable - which can't be chosen between at runtime, since hook calls can't
 * be conditional) and passes the result in here, so only the actual shared JSX/props-forwarding
 * lives in one place. */
export default function DraggableTaskCardShell({
  task,
  setNodeRef,
  attributes,
  listeners,
  style,
  className,
  ...handlers
}: DraggableTaskCardShellProps) {
  return (
    <div ref={setNodeRef} style={style} className={className}>
      <TaskCard task={task} dragHandleAttributes={attributes} dragHandleListeners={listeners} {...handlers} />
    </div>
  )
}
