'use client'

import React, { useEffect, useState } from 'react'
import { useAuth } from '@clerk/nextjs'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { getMoodEntries, deleteMoodEntry } from '@/lib/mood-api'
import { MOOD_OPTIONS } from '@/components/mood/MoodSelector'
import type { MoodEntryResponseDTO } from '@/types'

function getMoodOption(score: number) {
  return MOOD_OPTIONS.find((option) => option.value === score)
}

function formatEntryDateTime(dateTime: string): string {
  const date = new Date(dateTime)
  if (isNaN(date.getTime())) return dateTime
  return date.toLocaleString('en-GB', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function MoodHistory() {
  const { getToken } = useAuth()
  const [entries, setEntries] = useState<MoodEntryResponseDTO[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')
  const [isDeletingId, setIsDeletingId] = useState<number | null>(null)

  useEffect(() => {
    const loadEntries = async () => {
      setIsLoading(true)
      setErrorMessage('')
      try {
        const loadedEntries = await getMoodEntries(getToken)
        const sorted = [...loadedEntries].sort(
          (a, b) => new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime()
        )
        setEntries(sorted)
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Unable to load mood entries.'
        setErrorMessage(message)
      } finally {
        setIsLoading(false)
      }
    }

    void loadEntries()
  }, [])

  const handleDelete = async (id: number) => {
    setIsDeletingId(id)
    setErrorMessage('')
    try {
      await deleteMoodEntry(id, getToken)
      setEntries((currentEntries) => currentEntries.filter((entry) => entry.id !== id))
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete mood entry.'
      setErrorMessage(message)
    } finally {
      setIsDeletingId(null)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Your journal</CardTitle>
        <CardDescription>
          {entries.length} recorded {entries.length === 1 ? 'entry' : 'entries'}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {errorMessage ? (
          <p className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-800 dark:border-rose-900 dark:bg-rose-950 dark:text-rose-200">
            {errorMessage}
          </p>
        ) : null}

        {isLoading ? (
          <p className="text-sm text-muted-foreground">Loading entries...</p>
        ) : entries.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            No entries yet. Log a mood to start your journal.
          </p>
        ) : (
          <ul className="space-y-3">
            {entries.map((entry) => {
              const moodOption = getMoodOption(entry.moodScore)
              return (
                <li
                  key={entry.id}
                  className="rounded-md border border-slate-200 dark:border-slate-700 p-4 space-y-2"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-2">
                      <span className="text-2xl" aria-hidden="true">
                        {moodOption?.icon}
                      </span>
                      <div>
                        <p className="text-sm font-medium text-slate-900 dark:text-slate-100">
                          {moodOption?.label ?? `Mood: ${entry.moodScore}`}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {formatEntryDateTime(entry.dateTime)}
                        </p>
                      </div>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleDelete(entry.id)}
                      disabled={isDeletingId === entry.id}
                    >
                      {isDeletingId === entry.id ? 'Deleting...' : 'Delete'}
                    </Button>
                  </div>

                  {entry.factors && entry.factors.length > 0 ? (
                    <div className="flex flex-wrap gap-1.5">
                      {entry.factors.map((factor) => (
                        <Badge key={factor} variant="secondary">
                          {factor}
                        </Badge>
                      ))}
                    </div>
                  ) : null}

                  {entry.notes ? (
                    <p className="text-sm text-slate-700 dark:text-slate-300 whitespace-pre-wrap">
                      {entry.notes}
                    </p>
                  ) : null}
                </li>
              )
            })}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}
