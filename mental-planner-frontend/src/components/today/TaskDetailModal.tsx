'use client'

import { useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { History, ListChecks, Pause, Pencil, Play, Plus, Timer as TimerIcon, Trash2, X } from 'lucide-react'
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
import useTaskTimeEntries from '@/hooks/useTaskTimeEntries'
import type { SubtaskResponseDTO, TaskPriority, TaskResponseDTO, TaskTimeEntryResponseDTO } from '@/types'

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
  displayFormatter = (m: number) => formatLiveTime(m * 60),
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

function groupEntriesByDate(entries: TaskTimeEntryResponseDTO[]) {
  const groups = new Map<string, TaskTimeEntryResponseDTO[]>()
  for (const entry of entries) {
    const list = groups.get(entry.entryDate) ?? []
    list.push(entry)
    groups.set(entry.entryDate, list)
  }
  // Entries already arrive most-recent-day-first from the API, and Map preserves insertion order.
  return Array.from(groups.entries())
}

function todayDateKey() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

function TimeHistoryPanel({ taskId }: { taskId: number }) {
  const { entries, isLoading, addManualEntry, isAddingManualEntry, deleteEntry, isDeletingEntryId } =
    useTaskTimeEntries(taskId)
  const [manualDate, setManualDate] = useState(todayDateKey)
  const [manualMinutes, setManualMinutes] = useState('')
  const [manualNote, setManualNote] = useState('')

  const handleAddManual = (event: FormEvent) => {
    event.preventDefault()
    const minutes = Math.max(0, Number(manualMinutes) || 0)
    if (minutes <= 0) return
    addManualEntry({ entryDate: manualDate, minutes, note: manualNote.trim() || null })
    setManualMinutes('')
    setManualNote('')
  }

  const grouped = groupEntriesByDate(entries)

  return (
    <div className="ml-9 space-y-3 rounded-md border border-border/50 bg-muted/20 p-3">
      <form onSubmit={handleAddManual} className="flex flex-wrap items-end gap-2">
        <div className="flex flex-col gap-0.5">
          <label className="text-[10px] font-medium uppercase tracking-wide text-muted-foreground">Date</label>
          <Input
            type="date"
            value={manualDate}
            onChange={(event) => setManualDate(event.target.value)}
            className="h-7 w-36 border-none bg-muted/50 px-1.5 text-xs shadow-none focus-visible:ring-1"
          />
        </div>
        <div className="flex flex-col gap-0.5">
          <label className="text-[10px] font-medium uppercase tracking-wide text-muted-foreground">Minutes</label>
          <Input
            type="number"
            min={1}
            value={manualMinutes}
            onChange={(event) => setManualMinutes(event.target.value)}
            placeholder="0"
            className="h-7 w-20 border-none bg-muted/50 px-1.5 text-right text-xs shadow-none focus-visible:ring-1"
          />
        </div>
        <Input
          value={manualNote}
          onChange={(event) => setManualNote(event.target.value)}
          placeholder="Optional note"
          className="h-7 min-w-[120px] flex-1 border-none bg-muted/50 px-1.5 text-xs shadow-none focus-visible:ring-1"
        />
        <Button type="submit" size="sm" variant="secondary" className="h-7 gap-1 text-xs" disabled={isAddingManualEntry}>
          <Plus className="h-3 w-3" />
          Add
        </Button>
      </form>

      {isLoading ? (
        <p className="text-xs text-muted-foreground">Loading history...</p>
      ) : grouped.length === 0 ? (
        <p className="text-xs text-muted-foreground">No time logged yet.</p>
      ) : (
        <div className="space-y-2">
          {grouped.map(([date, dayEntries]) => (
            <div key={date}>
              <p className="text-[10px] font-medium uppercase tracking-wide text-muted-foreground">{date}</p>
              <ul className="mt-1 space-y-1">
                {dayEntries.map((entry) => (
                  <li key={entry.id} className="flex items-center gap-2 text-xs">
                    {entry.source === 'MANUAL' ? (
                      <Pencil className="h-3 w-3 shrink-0 text-muted-foreground" />
                    ) : (
                      <TimerIcon className="h-3 w-3 shrink-0 text-muted-foreground" />
                    )}
                    <span className="font-mono tabular-nums">{formatDuration(entry.minutes)}</span>
                    {entry.note && <span className="truncate text-muted-foreground">{entry.note}</span>}
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="ml-auto h-5 w-5 shrink-0"
                      onClick={() => deleteEntry(entry.id)}
                      disabled={isDeletingEntryId === entry.id}
                      aria-label="Delete time entry"
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}
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
  const [showHistory, setShowHistory] = useState(false)
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
          />
          <DurationField
            label="Planned"
            minutes={task.plannedMinutes ?? 0}
            onSave={(minutes) => onUpdate(task.id, { plannedMinutes: minutes })}
          />
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className={cn('h-8 w-8 rounded-full', showHistory && 'text-foreground')}
            onClick={() => setShowHistory((v) => !v)}
            aria-label={showHistory ? 'Hide time history' : 'View time history'}
          >
            <History className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {/* Time history */}
      {showHistory && <TimeHistoryPanel taskId={task.id} />}

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
