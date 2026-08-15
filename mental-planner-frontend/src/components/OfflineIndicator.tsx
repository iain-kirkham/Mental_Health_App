'use client'

import { useSyncExternalStore } from 'react'
import { onlineManager } from '@tanstack/react-query'
import { useIsMutating } from '@tanstack/react-query'
import { WifiOff } from 'lucide-react'

function subscribe(callback: () => void) {
  return onlineManager.subscribe(callback)
}

function getSnapshot() {
  return onlineManager.isOnline()
}

function getServerSnapshot() {
  return true
}

/** Shows when the app has lost real API reachability (see connectivity.ts), so any
 * mutations made in the meantime are paused rather than lost - the queued count reassures
 * the user those changes weren't silently dropped. */
export default function OfflineIndicator() {
  const isOnline = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot)
  const queuedMutations = useIsMutating()

  if (isOnline) return null

  return (
    <div className="flex items-center gap-1.5 rounded-full border border-transparent bg-chart-4/15 px-2.5 py-0.5 text-xs font-semibold text-chart-4">
      <WifiOff className="h-3 w-3" aria-hidden="true" />
      <span>Offline{queuedMutations > 0 ? ` (${queuedMutations} change${queuedMutations === 1 ? '' : 's'} queued)` : ''}</span>
    </div>
  )
}
