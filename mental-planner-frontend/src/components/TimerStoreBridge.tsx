'use client'

import { useCallback, useEffect } from 'react'
import { useAuth } from '@clerk/nextjs'
import { toast } from 'sonner'
import { logTimeEntry, updateActualMinutes } from '@/lib/tasks-api'
import { useTimerStore } from '@/store/timerStore'
import { beginFocusEntry } from '@/lib/focusEntryHandoff'
import { toFriendlyMessage } from '@/lib/connectivity'
import type { TimeEntrySource } from '@/types'

type LoggedRun = {
  taskId: number | null
  runKey: string
  startedAt: string
  endedAt: string
  minutes: number
  entryDate: string
  source: TimeEntrySource
}

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

  const logEntry = useCallback(
    (run: LoggedRun) => {
      // Registered synchronously (before the POST resolves) so a reflection submitted while
      // this is still in flight can still find it - see focusEntryHandoff.
      const entryIdPromise = (async (): Promise<number | null> => {
        try {
          const created = await logTimeEntry(
            {
              taskId: run.taskId,
              startedAt: run.startedAt,
              endedAt: run.endedAt,
              minutes: run.minutes,
              entryDate: run.entryDate,
              source: run.source,
              notes: null,
              score: null,
              energyRating: null,
            },
            getToken
          )
          return created.id
        } catch (error) {
          // Non-fatal: a lost history row shouldn't block the timer or the actualMinutes total,
          // which was already persisted separately via `persist` above.
          toast.error(toFriendlyMessage(error, 'Unable to save time entry.'))
          return null
        }
      })()

      beginFocusEntry(run.runKey, entryIdPromise)
    },
    [getToken]
  )

  useEffect(() => {
    useTimerStore.getState().setPersistFn(persist)
    useTimerStore.getState().setLogEntryFn(logEntry)
  }, [persist, logEntry])

  // localStorage isn't available during SSR, so the store is created with hydration skipped
  // (see timerStore.ts) and rehydrated here instead, once mounted client-side.
  useEffect(() => {
    void useTimerStore.persist.rehydrate()
  }, [])

  return null
}
