'use client'

import { useEffect } from 'react'
import { useCategoryColorStore } from '@/store/categoryColorStore'
import { CHANNEL_COLORS, defaultChannelColorIndex, normalizeCategoryKey, type ChannelColorEntry } from '@/lib/channel-color'

export interface ResolvedChannelColor extends ChannelColorEntry {
  /** Index into CHANNEL_COLORS this resolved to - lets a caller (e.g. the color picker) compare
   * against its own swatch loop without recomputing the resolution itself. */
  index: number
  /** Whether `index` came from a manual override rather than the hash-based default. */
  isOverride: boolean
}

/** Resolves a category to its color entry: a user-chosen override if one's been set for that
 * category name, otherwise the deterministic hash-based default. Pulled out from useChannelColor
 * so it's testable against a plain overrides object, with no store or render involved. */
export function resolveChannelColor(
  overrides: Record<string, number>,
  category: string | null | undefined
): ResolvedChannelColor {
  const key = normalizeCategoryKey(category ?? 'default') || 'default'
  const overrideIndex = overrides[key]
  const isOverride = overrideIndex !== undefined
  const index = isOverride ? overrideIndex : defaultChannelColorIndex(key)
  const entry = CHANNEL_COLORS[index] ?? CHANNEL_COLORS[0]
  return { ...entry, index, isOverride }
}

/** Overrides live in localStorage (via useCategoryColorStore) and are hydrated after mount, so
 * the very first render always matches the server-rendered hash-based color - no hydration
 * mismatch, just a same-tick swap to the override once it's loaded. */
export function useChannelColor(category: string | null | undefined): ResolvedChannelColor {
  const overrides = useCategoryColorStore((state) => state.overrides)
  const hydrate = useCategoryColorStore((state) => state.hydrate)
  useEffect(() => {
    hydrate()
  }, [hydrate])

  return resolveChannelColor(overrides, category)
}
