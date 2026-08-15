'use client'

import { useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import {
  Dialog,
  DialogContent,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import DatePickerButton from './DatePickerButton'
import PriorityPicker from './PriorityPicker'
import type { TaskPriority } from '@/types'

interface NewTaskModalProps {
  dateKey: string | null
  onOpenChange: (open: boolean) => void
  onCreate: (
    dateKey: string,
    title: string,
    details: { description: string | null; category: string | null; plannedMinutes: number | null; priority: TaskPriority }
  ) => void
}

function formatDuration(minutes: number) {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}:${String(mins).padStart(2, '0')}`
}

function NewTaskForm({
  dateKey,
  onCreate,
  onOpenChange,
}: {
  dateKey: string
  onCreate: NewTaskModalProps['onCreate']
  onOpenChange: (open: boolean) => void
}) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('')
  const [editingCategory, setEditingCategory] = useState(false)
  const [plannedMinutes, setPlannedMinutes] = useState('')
  const [editingPlanned, setEditingPlanned] = useState(false)
  const [priority, setPriority] = useState<TaskPriority>('NORMAL')
  const [selectedDate, setSelectedDate] = useState(dateKey)
  const notesRef = useRef<HTMLTextAreaElement | null>(null)

  const resizeNotes = (el: HTMLTextAreaElement | null) => {
    if (!el) return
    el.style.height = 'auto'
    el.style.height = `${el.scrollHeight}px`
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    if (!title.trim()) return

    onCreate(selectedDate, title, {
      description: description.trim() || null,
      category: category.trim() || null,
      plannedMinutes: plannedMinutes.trim() === '' ? null : Math.max(0, Number(plannedMinutes)),
      priority,
    })
    onOpenChange(false)
  }

  const handleTitleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault()
      handleSubmit(event as unknown as FormEvent)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <DialogTitle className="sr-only">Add task</DialogTitle>

      {/* Metadata bar */}
      <div className="flex items-center justify-between gap-3 pr-6">
        {editingCategory ? (
          <Input
            autoFocus
            value={category}
            onChange={(event) => setCategory(event.target.value)}
            onBlur={() => setEditingCategory(false)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') setEditingCategory(false)
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
            {category || 'Add category'}
          </button>
        )}

        <div className="flex items-center gap-1">
          <PriorityPicker value={priority} onChange={setPriority} />

          {editingPlanned ? (
            <Input
              type="number"
              min={0}
              autoFocus
              value={plannedMinutes}
              onChange={(event) => setPlannedMinutes(event.target.value)}
              onBlur={() => setEditingPlanned(false)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') setEditingPlanned(false)
              }}
              placeholder="min"
              className="h-7 w-20 rounded-sm border-none bg-muted/50 px-2 text-right font-mono text-xs shadow-none focus-visible:ring-1"
            />
          ) : (
            <button
              type="button"
              onClick={() => setEditingPlanned(true)}
              className="rounded px-2 py-1 font-mono text-xs font-medium text-muted-foreground hover:bg-muted/50 hover:text-foreground"
            >
              {plannedMinutes.trim() !== '' ? formatDuration(Number(plannedMinutes)) : 'Add time'}
            </button>
          )}

          <DatePickerButton value={selectedDate} onChange={setSelectedDate} />
        </div>
      </div>

      {/* Frameless title */}
      <input
        value={title}
        onChange={(event) => setTitle(event.target.value)}
        onKeyDown={handleTitleKeyDown}
        placeholder="Task title"
        aria-label="Task title"
        autoFocus
        className="w-full border-none bg-transparent p-0 text-2xl font-semibold text-foreground outline-none placeholder:text-overlay0 focus-visible:ring-0"
      />

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
        placeholder="Write notes..."
        className="min-h-[100px] resize-none overflow-hidden border-none bg-transparent p-0 text-sm leading-relaxed text-foreground shadow-none focus-visible:ring-0"
      />

      <div className="flex justify-end">
        <Button type="submit" disabled={!title.trim()}>
          Add task
        </Button>
      </div>
    </form>
  )
}

export default function NewTaskModal({ dateKey, onOpenChange, onCreate }: NewTaskModalProps) {
  return (
    <Dialog open={dateKey !== null} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto p-6 sm:p-8">
        {dateKey && <NewTaskForm key={dateKey} dateKey={dateKey} onCreate={onCreate} onOpenChange={onOpenChange} />}
      </DialogContent>
    </Dialog>
  )
}
