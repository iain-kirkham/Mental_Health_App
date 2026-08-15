import type { TaskPriority } from '@/types'

export const PRIORITY_OPTIONS: { value: TaskPriority; label: string }[] = [
  { value: 'URGENT', label: 'Urgent' },
  { value: 'HIGH', label: 'High priority' },
  { value: 'NORMAL', label: 'Normal' },
  { value: 'LOW', label: 'Low priority' },
]

export function priorityLabel(priority: TaskPriority): string {
  return PRIORITY_OPTIONS.find((option) => option.value === priority)?.label ?? 'Normal'
}

// Normal is the common case, so it's left unmarked to keep the board flat and low-noise -
// only priorities that actually need attention get a color.
export function priorityDotClass(priority: TaskPriority): string {
  switch (priority) {
    case 'URGENT':
      return 'bg-destructive'
    case 'HIGH':
      return 'bg-chart-4'
    case 'LOW':
      return 'bg-chart-1'
    default:
      return 'bg-muted-foreground/40'
  }
}

export function priorityBorderClass(priority: TaskPriority): string {
  switch (priority) {
    case 'URGENT':
      return 'border-l-2 border-l-destructive'
    case 'HIGH':
      return 'border-l-2 border-l-chart-4'
    case 'LOW':
      return 'border-l-2 border-l-chart-1'
    default:
      return ''
  }
}
