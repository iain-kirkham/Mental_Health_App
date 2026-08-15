'use client'

import { useState } from 'react'
import { format, parseISO } from 'date-fns'
import { CalendarIcon } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { cn } from '@/lib/utils'

interface DatePickerButtonProps {
  value: string
  onChange: (dateKey: string) => void
  className?: string
  showLabel?: boolean
  ariaLabel?: string
}

export default function DatePickerButton({ value, onChange, className, showLabel = true, ariaLabel }: DatePickerButtonProps) {
  const [open, setOpen] = useState(false)
  const selected = parseISO(value)

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="ghost"
          size={showLabel ? 'sm' : 'icon'}
          aria-label={ariaLabel ?? 'Pick a date'}
          className={cn(
            showLabel
              ? 'h-7 gap-1.5 px-2 text-xs font-medium text-muted-foreground hover:bg-muted/50 hover:text-foreground'
              : 'h-7 w-7 text-muted-foreground hover:bg-muted/50 hover:text-foreground',
            className
          )}
        >
          <CalendarIcon className="h-3.5 w-3.5" />
          {showLabel && format(selected, 'EEE, MMM d')}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0" align="end">
        <Calendar
          mode="single"
          selected={selected}
          onSelect={(date) => {
            if (!date) return
            onChange(format(date, 'yyyy-MM-dd'))
            setOpen(false)
          }}
        />
      </PopoverContent>
    </Popover>
  )
}
