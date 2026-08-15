import React from 'react'
import OfflineIndicator from '@/components/OfflineIndicator'

type Props = {
  /** Short page title, rendered as the page's h1. */
  title: React.ReactNode
  /** Optional trailing controls (links, buttons) aligned to the right. */
  children?: React.ReactNode
}

/**
 * The thin bar that sits directly under the navbar on every page. Keeping it in
 * one place means header height, border, and heading typography stay in step
 * across the app.
 */
export default function PageHeader({ title, children }: Props) {
  return (
    <div className="flex shrink-0 items-center justify-between border-b border-border px-4 py-2 md:px-6">
      <h1 className="text-sm font-semibold text-foreground">{title}</h1>
      <div className="flex items-center gap-3">
        <OfflineIndicator />
        {children}
      </div>
    </div>
  )
}
