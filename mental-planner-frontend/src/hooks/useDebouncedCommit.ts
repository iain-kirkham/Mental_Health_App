'use client'

import { useEffect, useRef } from 'react'

/**
 * Returns a `commit` function that fires `onCommit(value)` after `delayMs` of inactivity,
 * cancelling any pending commit from a previous call first - so a fast typist triggers one
 * commit instead of one per keystroke. `onCommit` is taken per-call (not bound once) so it can
 * close over per-call state (e.g. which task a note belongs to) rather than whatever's current
 * by the time the timer fires. Unmounting cancels a pending commit without firing it.
 */
export function useDebouncedCommit(delayMs: number) {
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    return () => {
      if (timeoutRef.current) clearTimeout(timeoutRef.current)
    }
  }, [])

  return function commit<TValue>(value: TValue, onCommit: (value: TValue) => void) {
    if (timeoutRef.current) clearTimeout(timeoutRef.current)
    timeoutRef.current = setTimeout(() => onCommit(value), delayMs)
  }
}
