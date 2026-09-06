'use client'

import { useState } from 'react'

/**
 * State scoped to whatever `key` currently is - reading returns `fallback` whenever the stored
 * value belongs to a different key (or `key` is null), so switching keys (e.g. moving to a
 * different task in a queue that keeps the same component mounted) never leaks the previous
 * key's draft onto the new one. Setting attaches the current `key` to the value; `reset` clears
 * the stored value outright, falling back immediately.
 */
export function useKeyedDraft<TKey, TValue>(
  key: TKey | null,
  fallback: TValue
): [TValue, (value: TValue) => void, () => void] {
  const [stored, setStored] = useState<{ key: TKey; value: TValue } | null>(null)

  const value = stored !== null && key !== null && stored.key === key ? stored.value : fallback

  const setValue = (next: TValue) => {
    if (key === null) return
    setStored({ key, value: next })
  }

  const reset = () => setStored(null)

  return [value, setValue, reset]
}
