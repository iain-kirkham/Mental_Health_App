'use client'

import { useSyncExternalStore } from 'react'

function subscribe(query: string) {
  return (callback: () => void) => {
    const mql = window.matchMedia(query)
    mql.addEventListener('change', callback)
    return () => mql.removeEventListener('change', callback)
  }
}

/** SSR-safe media query match. Server/first-paint snapshot is always `false` (matching
 * useHasMounted's pattern elsewhere) to avoid a hydration mismatch - it flips to the real value
 * immediately after mount, same tradeoff as any other client-only layout read. */
export function useMediaQuery(query: string): boolean {
  return useSyncExternalStore(
    subscribe(query),
    () => window.matchMedia(query).matches,
    () => false
  )
}
