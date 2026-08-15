'use client'

import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { PRIORITY_OPTIONS, priorityDotClass, priorityLabel } from '@/lib/task-priority'
import { cn } from '@/lib/utils'
import type { TaskPriority } from '@/types'

interface PriorityPickerProps {
  value: TaskPriority
  onChange: (priority: TaskPriority) => void
  className?: string
}

export default function PriorityPicker({ value, onChange, className }: PriorityPickerProps) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button
          type="button"
          className={cn(
            'flex h-7 items-center gap-1.5 rounded px-2 text-xs font-medium text-muted-foreground hover:bg-muted/50 hover:text-foreground',
            className
          )}
        >
          <span className={cn('h-1.5 w-1.5 shrink-0 rounded-full', priorityDotClass(value))} aria-hidden />
          {priorityLabel(value)}
        </button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-40">
        {PRIORITY_OPTIONS.map((option) => (
          <DropdownMenuItem key={option.value} onClick={() => onChange(option.value)} className="gap-1.5">
            <span className={cn('h-1.5 w-1.5 shrink-0 rounded-full', priorityDotClass(option.value))} aria-hidden />
            {option.label}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
