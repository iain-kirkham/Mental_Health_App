'use client'

import { useCallback, useEffect } from 'react'
import { useAuth } from '@clerk/nextjs'
import { toast } from 'sonner'
import { updateActualMinutes } from '@/lib/tasks-api'
import { useTimerStore } from '@/store/timerStore'
import { toFriendlyMessage } from '@/lib/connectivity'

/**
 * Mounted once at the app root so the global task stopwatch (useTimerStore) can persist
 * accumulated time to the backend regardless of which page is currently active - the store
 * itself has no access to Clerk auth, so it calls back into this bridge to do the write.
 *
 * Uses the dedicated actual-minutes-only endpoint (not a full task PUT) so a pause/stop
 * persist can never clobber other fields - e.g. completion state - that changed
 * concurrently elsewhere while the timer was running.
 */
export function TimerStoreBridge() {
  const { getToken } = useAuth()

  const persist = useCallback(
    (taskId: number, actualMinutes: number) => {
      void (async () => {
        try {
          const updated = await updateActualMinutes(taskId, actualMinutes, getToken)
          useTimerStore.getState().setLastPersistedTask(updated)
        } catch (error) {
          toast.error(toFriendlyMessage(error, 'Unable to save tracked time.'))
        }
      })()
    },
    [getToken]
  )

  useEffect(() => {
    useTimerStore.getState().setPersistFn(persist)
  }, [persist])

  return null
}
