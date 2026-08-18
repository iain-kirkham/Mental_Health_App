'use client'

import { useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { ListChecks, Pause, Play, Trash2, X } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import DatePickerButton from './DatePickerButton'
import PriorityPicker from './PriorityPicker'
import { cn } from '@/lib/utils'
import { useTimerStore } from '@/store/timerStore'
import type { SubtaskResponseDTO, TaskPriority, TaskResponseDTO } from '@/types'

interface TaskDetailModalProps {
  task: TaskResponseDTO | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onUpdate: (id: number, changes: { title?: string; description?: string | null; category?: string | null; plannedMinutes?: number | null; actualMinutes?: number; priority?: TaskPriority }) => void
  onMoveDay: (id: number, destDateKey: string) => void
  onDelete: (id: number) => void
  onToggleComplete: (id: number, completed: boolean) => void
  onAddSubtask: (taskId: number, title: string) => void
  onToggleSubtask: (taskId: number, subtaskId: number, completed: boolean) => void
  onDeleteSubtask: (taskId: number, subtaskId: number) => void
  onUpdateSubtask: (taskId: number, subtaskId: number, changes: { title?: string; plannedMinutes?: number | null }) => void
}

type TaskDetailFormProps = Omit<TaskDetailModalProps, 'open' | 'onOpenChange' | 'task'> & { task: TaskResponseDTO }

function formatDuration(minutes: number) {
  if (minutes <= 0) return '0:00'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}:${String(mins).padStart(2, '0')}`
}

function formatLiveTime(totalSeconds: number) {
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
}

const CHECKED_GREEN = 'data-[state=checked]:bg-chart-2 data-[state=checked]:border-chart-2'

function DurationField({
  label,
  minutes,
  onSave,
  liveDisplay,
  displayFormatter = formatDuration,
}: {
  label: string
  minutes: number
  onSave: (minutes: number) => void
  liveDisplay?: string | null
  displayFormatter?: (minutes: number) => string
}) {
  const [editing, setEditing] = useState(false)
  const [value, setValue] = useState(String(minutes))

  const commit = () => {
    setEditing(false)
    const parsed = Math.max(0, Number(value) || 0)
    if (parsed !== minutes) onSave(parsed)
  }

  return (
    <div className="flex flex-col items-end gap-0.5">
      <span className="text-[10px] font-medium uppercase tracking-wide text-muted-foreground">{label}</span>
      {liveDisplay != null ? (
        <span className="rounded-sm px-1 font-mono text-sm font-semibold tabular-nums text-chart-2">{liveDisplay}</span>
      ) : editing ? (
        <Input
          type="number"
          min={0}
          autoFocus
          value={value}
          onChange={(event) => setValue(event.target.value)}
          onBlur={commit}
          onKeyDown={(event) => {
            if (event.key === 'Enter') commit()
          }}
          className="h-6 w-16 rounded-sm border-none bg-muted/50 px-1.5 text-right font-mono text-sm shadow-none focus-visible:ring-1"
        />
      ) : (
        <button
          type="button"
          onClick={() => {
            setValue(String(minutes))
            setEditing(true)
          }}
          className="rounded-sm px-1 font-mono text-sm font-medium tabular-nums text-foreground hover:bg-muted/50"
        >
          {displayFormatter(minutes)}
        </button>
      )}
    </div>
  )
}

function SubtaskRow({
  subtask,
  onToggle,
  onDelete,
  onUpdate,
}: {
  subtask: SubtaskResponseDTO
  onToggle: (completed: boolean) => void
  onDelete: () => void
  onUpdate: (changes: { title?: string; plannedMinutes?: number | null }) => void
}) {
  const [editingMinutes, setEditingMinutes] = useState(false)
  const [minutesValue, setMinutesValue] = useState(subtask.plannedMinutes != null ? String(subtask.plannedMinutes) : '')

  const commitMinutes = () => {
    setEditingMinutes(false)
    const next = minutesValue.trim() === '' ? null : Math.max(0, Number(minutesValue) || 0)
    if (next !== subtask.plannedMinutes) onUpdate({ plannedMinutes: next })
  }

  return (
    <div
      className={cn(
        'group flex items-center gap-2.5 transition-opacity duration-300 ease-in-out',
        subtask.completed ? 'opacity-60' : 'opacity-100'
      )}
    >
      <Checkbox
        checked={subtask.completed}
        onCheckedChange={(checked) => onToggle(checked === true)}
        className={cn('h-4 w-4 rounded-full', CHECKED_GREEN)}
      />
      <input
        defaultValue={subtask.title}
        onBlur={(event) => {
          const trimmed = event.target.value.trim()
          if (trimmed && trimmed !== subtask.title) onUpdate({ title: trimmed })
        }}
        className={cn(
          'flex-1 border-none bg-transparent p-0 text-sm outline-none transition-colors duration-300 ease-in-out focus-visible:ring-0',
          subtask.completed ? 'text-muted-foreground line-through' : 'text-foreground'
        )}
      />
      {editingMinutes ? (
        <Input
          type="number"
          min={0}
          autoFocus
          value={minutesValue}
          onChange={(event) => setMinutesValue(event.target.value)}
          onBlur={commitMinutes}
          onKeyDown={(event) => {
            if (event.key === 'Enter') commitMinutes()
          }}
          className="h-6 w-14 rounded-sm border-none bg-muted/50 px-1.5 text-right font-mono text-xs shadow-none focus-visible:ring-1"
        />
      ) : (
        <button
          type="button"
          onClick={() => setEditingMinutes(true)}
          className="shrink-0 rounded-sm px-1 font-mono text-xs tabular-nums text-muted-foreground opacity-0 hover:bg-muted/50 group-hover:opacity-100"
        >
          {subtask.plannedMinutes != null ? formatDuration(subtask.plannedMinutes) : '+ time'}
        </button>
      )}
      <Button
        type="button"
        variant="ghost"
        size="icon"
        className="h-6 w-6 shrink-0 opacity-0 group-hover:opacity-100"
        onClick={onDelete}
        aria-label="Delete subtask"
      >
        <X className="h-3 w-3" />
      </Button>
    </div>
  )
}

function TaskDetailForm({
  task,
  onUpdate,
  onMoveDay,
  onDelete,
  onToggleComplete,
  onAddSubtask,
  onToggleSubtask,
  onDeleteSubtask,
  onUpdateSubtask,
}: TaskDetailFormProps) {
  const activeTaskId = useTimerStore((state) => state.activeTaskId)
  const isRunning = useTimerStore((state) => state.isRunning)
  const elapsedSeconds = useTimerStore((state) => state.elapsedSeconds)
  const startTimer = useTimerStore((state) => state.startTimer)
  const pauseTimer = useTimerStore((state) => state.pauseTimer)

  const isTimerRunning = isRunning && activeTaskId === task.id
  const liveSeconds = isTimerRunning ? task.actualMinutes * 60 + elapsedSeconds : null

  const [title, setTitle] = useState(task.title)
  const [description, setDescription] = useState(task.description ?? '')
  const [category, setCategory] = useState(task.category ?? '')
  const [editingCategory, setEditingCategory] = useState(false)
  const [showSubtasks, setShowSubtasks] = useState(task.subtasks.length > 0)
  const [subtaskTitle, setSubtaskTitle] = useState('')
  const notesRef = useRef<HTMLTextAreaElement | null>(null)

  const resizeNotes = (el: HTMLTextAreaElement | null) => {
    if (!el) return
    el.style.height = 'auto'
    el.style.height = `${el.scrollHeight}px`
  }

  const handleTitleBlur = () => {
    const trimmed = title.trim()
    if (trimmed && trimmed !== task.title) onUpdate(task.id, { title: trimmed })
  }

  const handleTitleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') event.currentTarget.blur()
  }

  const handleDescriptionBlur = () => {
    const next = description.trim() || null
    if (next !== task.description) onUpdate(task.id, { description: next })
  }

  const commitCategory = () => {
    setEditingCategory(false)
    const next = category.trim() || null
    if (next !== task.category) onUpdate(task.id, { category: next })
  }

  const handleAddSubtask = (event: FormEvent) => {
    event.preventDefault()
    if (!subtaskTitle.trim()) return
    onAddSubtask(task.id, subtaskTitle)
    setSubtaskTitle('')
  }

  return (
    <div className="space-y-5">
      <DialogTitle className="sr-only">{task.title}</DialogTitle>
      {/* Metadata bar */}
      <div className="flex items-center justify-between gap-3 pr-6">
        {editingCategory ? (
          <Input
            autoFocus
            value={category}
            onChange={(event) => setCategory(event.target.value)}
            onBlur={commitCategory}
            onKeyDown={(event) => {
              if (event.key === 'Enter') commitCategory()
            }}
            placeholder="category"
            className="h-7 w-40 border-none bg-muted/50 px-2 text-xs shadow-none focus-visible:ring-1"
          />
        ) : (
          <button
            type="button"
            onClick={() => setEditingCategory(true)}
            className="rounded px-2 py-1 text-xs font-medium text-muted-foreground hover:bg-muted/50 hover:text-foreground"
          >
            {task.category || 'Add category'}
          </button>
        )}

        <div className="flex items-center gap-1">
          <PriorityPicker value={task.priority} onChange={(priority) => onUpdate(task.id, { priority })} />

          <DatePickerButton value={task.scheduledDate} onChange={(dateKey) => onMoveDay(task.id, dateKey)} />

          <Button
            type="button"
            variant="ghost"
            size="sm"
            className={cn('h-7 gap-1.5 px-2 text-xs font-medium', showSubtasks ? 'text-foreground' : 'text-muted-foreground')}
            onClick={() => setShowSubtasks((v) => !v)}
          >
            <ListChecks className="h-3.5 w-3.5" />
            Subtasks
          </Button>
        </div>
      </div>

      {/* Frameless header */}
      <div className="flex items-start justify-between gap-4">
        <div className="flex flex-1 items-center gap-3">
          <Checkbox
            checked={task.completed}
            onCheckedChange={(checked) => onToggleComplete(task.id, checked === true)}
            className={cn('h-6 w-6 rounded-full', CHECKED_GREEN)}
          />
          <input
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            onBlur={handleTitleBlur}
            onKeyDown={handleTitleKeyDown}
            className={cn(
              'flex-1 border-none bg-transparent p-0 text-2xl font-semibold text-foreground outline-none transition-colors duration-300 ease-in-out focus-visible:ring-0',
              task.completed && 'text-muted-foreground line-through'
            )}
            aria-label="Task title"
          />
        </div>

        <div className="flex shrink-0 items-center gap-3 pt-1">
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className={cn('h-8 w-8 rounded-full', isTimerRunning && 'text-chart-2')}
            onClick={() => (isTimerRunning ? pauseTimer() : startTimer(task.id, task.actualMinutes))}
            aria-label={isTimerRunning ? 'Pause timer' : 'Start timer'}
          >
            {isTimerRunning ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
          </Button>
          <DurationField
            label="Actual"
            minutes={task.actualMinutes}
            onSave={(minutes) => onUpdate(task.id, { actualMinutes: minutes })}
            liveDisplay={isTimerRunning && liveSeconds != null ? formatLiveTime(liveSeconds) : null}
            displayFormatter={(minutes) => formatLiveTime(minutes * 60)}
          />
          <DurationField
            label="Planned"
            minutes={task.plannedMinutes ?? 0}
            onSave={(minutes) => onUpdate(task.id, { plannedMinutes: minutes })}
          />
        </div>
      </div>

      {/* Inline subtasks */}
      {showSubtasks && (
        <div className="ml-9 space-y-2">
          {task.subtasks.map((subtask) => (
            <SubtaskRow
              key={subtask.id}
              subtask={subtask}
              onToggle={(completed) => onToggleSubtask(task.id, subtask.id, completed)}
              onDelete={() => onDeleteSubtask(task.id, subtask.id)}
              onUpdate={(changes) => onUpdateSubtask(task.id, subtask.id, changes)}
            />
          ))}
          <form onSubmit={handleAddSubtask}>
            <input
              value={subtaskTitle}
              onChange={(event) => setSubtaskTitle(event.target.value)}
              onBlur={() => {
                if (subtaskTitle.trim()) onAddSubtask(task.id, subtaskTitle)
                setSubtaskTitle('')
              }}
              placeholder="+ Add subtask"
              className="w-full border-none bg-transparent p-0 text-sm text-muted-foreground outline-none placeholder:text-muted-foreground focus-visible:ring-0"
            />
          </form>
        </div>
      )}

      {/* Notes */}
      <Textarea
        ref={(el) => {
          notesRef.current = el
          resizeNotes(el)
        }}
        value={description}
        onChange={(event) => {
          setDescription(event.target.value)
          resizeNotes(event.target)
        }}
        onBlur={handleDescriptionBlur}
        placeholder="Write notes..."
        className="min-h-[140px] resize-none overflow-hidden border-none bg-transparent p-0 text-sm leading-relaxed text-foreground shadow-none focus-visible:ring-0"
      />

      {/* Actions */}
      <div className="flex items-center justify-end gap-1">
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="gap-1.5 text-xs text-muted-foreground hover:text-destructive"
          onClick={() => onDelete(task.id)}
        >
          <Trash2 className="h-3.5 w-3.5" />
          Delete
        </Button>
      </div>
    </div>
  )
}

export default function TaskDetailModal({ task, open, onOpenChange, ...formProps }: TaskDetailModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto p-6 sm:p-8">
        {task && <TaskDetailForm key={task.id} task={task} {...formProps} />}
      </DialogContent>
    </Dialog>
  )
}
