'use client'

import { useMemo, useState } from 'react'
import { useAuth } from '@clerk/nextjs'
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragStartEvent,
} from '@dnd-kit/core'
import { addDays, addWeeks, format, parseISO, startOfWeek, subWeeks } from 'date-fns'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import DayColumn from '@/components/today/DayColumn'
import TaskCard from '@/components/today/TaskCard'
import TaskDetailModal from '@/components/today/TaskDetailModal'
import FocusModeOverlay from '@/components/today/FocusModeOverlay'
import NewTaskModal from '@/components/today/NewTaskModal'
import DatePickerButton from '@/components/today/DatePickerButton'
import PageHeader from '@/components/PageHeader'
import useTasksForWeek from '@/hooks/useTasksForWeek'
import type { TaskResponseDTO } from '@/types'

function toDateKey(date: Date) {
  return format(date, 'yyyy-MM-dd')
}

export default function PlannerPage() {
  const { getToken } = useAuth()
  const [weekAnchor, setWeekAnchor] = useState(() => new Date())
  const weekStart = useMemo(() => startOfWeek(weekAnchor, { weekStartsOn: 1 }), [weekAnchor])
  const weekDays = useMemo(() => Array.from({ length: 7 }, (_, i) => addDays(weekStart, i)), [weekStart])
  const startDateKey = toDateKey(weekDays[0])
  const endDateKey = toDateKey(weekDays[6])

  const {
    tasks,
    isLoading,
    addTask,
    updateTask,
    setTaskCompletion,
    removeTask,
    moveTask,
    addSubtask,
    updateSubtaskItem,
    removeSubtaskItem,
  } = useTasksForWeek(startDateKey, endDateKey, getToken)

  const columns = useMemo(() => {
    const grouped = new Map<string, TaskResponseDTO[]>()
    for (const day of weekDays) grouped.set(toDateKey(day), [])
    for (const task of tasks) {
      grouped.get(task.scheduledDate)?.push(task)
    }
    for (const list of grouped.values()) list.sort((a, b) => a.sortOrder - b.sortOrder)
    return grouped
  }, [tasks, weekDays])

  const [activeTask, setActiveTask] = useState<TaskResponseDTO | null>(null)
  const [selectedTaskId, setSelectedTaskId] = useState<number | null>(null)
  const selectedTask = tasks.find((task) => task.id === selectedTaskId) ?? null
  const [addTaskDateKey, setAddTaskDateKey] = useState<string | null>(null)
  const [focusTaskId, setFocusTaskId] = useState<number | null>(null)
  const focusTask = tasks.find((task) => task.id === focusTaskId) ?? null

  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }))

  const handleToggleComplete = (id: number, completed: boolean) => {
    void setTaskCompletion(id, completed)
  }

  const handleDragStart = (event: DragStartEvent) => {
    const taskId = Number(event.active.id)
    setActiveTask(tasks.find((task) => task.id === taskId) ?? null)
  }

  const handleDragEnd = (event: DragEndEvent) => {
    setActiveTask(null)
    const { active, over } = event
    if (!over) return

    const taskId = Number(active.id)
    if (Number.isNaN(taskId)) return

    const overData = over.data.current as { dateKey?: string } | undefined
    const destDateKey = overData?.dateKey ?? String(over.id)
    if (!columns.has(destDateKey)) return

    const destColumn = columns.get(destDateKey) ?? []
    const overTaskIndex = destColumn.findIndex((task) => String(task.id) === String(over.id))
    const destIndex = overTaskIndex === -1 ? destColumn.length : overTaskIndex

    void moveTask(taskId, destDateKey, destIndex)
  }

  return (
    <main className="flex h-[calc(100vh-4rem)] flex-col">
      <PageHeader title="Planner">
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => setWeekAnchor((d) => subWeeks(d, 1))} aria-label="Previous week">
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="text-xs font-medium text-muted-foreground">
            {format(weekDays[0], 'MMM d')} – {format(weekDays[6], 'MMM d, yyyy')}
          </span>
          <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => setWeekAnchor((d) => addWeeks(d, 1))} aria-label="Next week">
            <ChevronRight className="h-4 w-4" />
          </Button>
          <DatePickerButton
            value={toDateKey(weekAnchor)}
            onChange={(dateKey) => setWeekAnchor(parseISO(dateKey))}
            showLabel={false}
            ariaLabel="Jump to date"
            className="ml-1"
          />
        </div>
      </PageHeader>

      {/* Left: kanban board. Right: reserved space for a future collapsible hourly time-blocking sidebar. */}
      <div className="flex min-h-0 flex-1 gap-3 px-3 py-3 md:px-4">
        <div className="min-w-0 flex-1">
          <DndContext sensors={sensors} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
            <div className="grid h-full grid-cols-1 gap-3 overflow-x-auto sm:grid-cols-2 lg:grid-cols-7">
              {weekDays.map((day) => {
                const dateKey = toDateKey(day)
                return (
                  <DayColumn
                    key={dateKey}
                    dateKey={dateKey}
                    date={day}
                    tasks={columns.get(dateKey) ?? []}
                    isLoading={isLoading}
                    onRequestAddTask={setAddTaskDateKey}
                    onToggleComplete={handleToggleComplete}
                    onToggleSubtask={(taskId, subtaskId, completed) => void updateSubtaskItem(taskId, subtaskId, { completed })}
                    onAddSubtask={(taskId, title) => void addSubtask(taskId, title)}
                    onOpenDetail={(task) => setSelectedTaskId(task.id)}
                    onOpenFocus={(task) => setFocusTaskId(task.id)}
                  />
                )
              })}
            </div>

            <DragOverlay>
              {activeTask ? (
                <TaskCard
                  task={activeTask}
                  onToggleComplete={() => {}}
                  onToggleSubtask={() => {}}
                  onAddSubtask={() => {}}
                  onOpenDetail={() => {}}
                  onOpenFocus={() => {}}
                  isOverlay
                />
              ) : null}
            </DragOverlay>
          </DndContext>
        </div>
      </div>

      <TaskDetailModal
        task={selectedTask}
        open={selectedTaskId !== null}
        onOpenChange={(open) => {
          if (!open) setSelectedTaskId(null)
        }}
        onUpdate={(id, changes) => void updateTask(id, changes)}
        onMoveDay={(id, destDateKey) => void moveTask(id, destDateKey, columns.get(destDateKey)?.length ?? 0)}
        onDelete={(id) => {
          void removeTask(id)
          setSelectedTaskId(null)
        }}
        onToggleComplete={handleToggleComplete}
        onAddSubtask={(taskId, title) => void addSubtask(taskId, title)}
        onToggleSubtask={(taskId, subtaskId, completed) => void updateSubtaskItem(taskId, subtaskId, { completed })}
        onDeleteSubtask={(taskId, subtaskId) => void removeSubtaskItem(taskId, subtaskId)}
        onUpdateSubtask={(taskId, subtaskId, changes) => void updateSubtaskItem(taskId, subtaskId, changes)}
        onOpenFocus={(task) => {
          setSelectedTaskId(null)
          setFocusTaskId(task.id)
        }}
      />

      <NewTaskModal
        dateKey={addTaskDateKey}
        onOpenChange={(open) => {
          if (!open) setAddTaskDateKey(null)
        }}
        onCreate={(dateKey, title, details) => void addTask(dateKey, title, details)}
      />

      <FocusModeOverlay
        task={focusTask}
        onOpenChange={(open) => {
          if (!open) setFocusTaskId(null)
        }}
      />
    </main>
  )
}
