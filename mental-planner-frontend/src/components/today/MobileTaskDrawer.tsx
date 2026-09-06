'use client'

import * as DialogPrimitive from '@radix-ui/react-dialog'
import { X } from 'lucide-react'

interface MobileTaskDrawerProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  children: React.ReactNode
}

/** Slide-out panel for the Today view's task queue on narrow screens, where there isn't room to
 * show the queue and the time-blocking grid side by side. Opens over the grid rather than
 * replacing it, so the grid stays mounted (and droppable) underneath the whole time. */
export default function MobileTaskDrawer({ open, onOpenChange, children }: MobileTaskDrawerProps) {
  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-40 bg-background/80 data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:animate-in data-[state=open]:fade-in-0" />
        <DialogPrimitive.Content
          className="fixed inset-y-0 left-0 z-50 flex w-[85vw] max-w-sm flex-col border-r border-border bg-background p-3 shadow-xl outline-none data-[state=closed]:animate-out data-[state=closed]:slide-out-to-left data-[state=open]:animate-in data-[state=open]:slide-in-from-left"
        >
          <DialogPrimitive.Title className="sr-only">Today&apos;s tasks</DialogPrimitive.Title>
          <DialogPrimitive.Close
            className="absolute right-2 top-2 rounded-full p-1.5 text-muted-foreground hover:bg-accent hover:text-foreground"
            aria-label="Close task list"
          >
            <X className="h-4 w-4" />
          </DialogPrimitive.Close>
          <div className="min-h-0 flex-1 pt-6">{children}</div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  )
}
