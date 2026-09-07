'use client'

import React, { useEffect, useState } from 'react'
import { useAuth } from '@clerk/nextjs'
import { format } from 'date-fns'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { getTimeEntriesInRange, deleteTimeEntry } from '@/lib/tasks-api'
import { toFriendlyMessage } from '@/lib/connectivity'
import { getScoreEmoji, getEnergyRatingOption } from '@/lib/focus-format'
import { formatDateTime } from '@/lib/utils'
import type { TaskTimeEntryResponseDTO } from '@/types'

// Every Focus session ever logged, regardless of date - there's no natural "recent window" for
// a session-history page the way there is for a day view, so the range is intentionally wide.
const HISTORY_RANGE_START = '1970-01-01'

function EnergyBadge({ energyRating }: { energyRating: TaskTimeEntryResponseDTO['energyRating'] }) {
    const option = getEnergyRatingOption(energyRating)
    if (!option) return null

    return (
        <Badge variant="outline" className={option.badgeClass}>
            {option.icon} {option.label}
        </Badge>
    )
}

export default function FocusHistory() {
    const { getToken } = useAuth()
    const [sessions, setSessions] = useState<TaskTimeEntryResponseDTO[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [errorMessage, setErrorMessage] = useState('')
    const [isDeletingId, setIsDeletingId] = useState<number | null>(null)

    useEffect(() => {
        const loadSessions = async () => {
            setIsLoading(true)
            setErrorMessage('')
            try {
                const today = format(new Date(), 'yyyy-MM-dd')
                const entries = await getTimeEntriesInRange(HISTORY_RANGE_START, today, getToken)
                const focusSessions = entries
                    .filter((entry) => entry.source === 'FOCUS')
                    .sort((a, b) => new Date(b.endedAt ?? b.entryDate).getTime() - new Date(a.endedAt ?? a.entryDate).getTime())
                setSessions(focusSessions)
            } catch (error) {
                setErrorMessage(toFriendlyMessage(error, 'Unable to load focus sessions.'))
            } finally {
                setIsLoading(false)
            }
        }

        void loadSessions()
    }, [])

    const handleDelete = async (id: number) => {
        setIsDeletingId(id)
        setErrorMessage('')
        try {
            await deleteTimeEntry(id, getToken)
            setSessions((currentSessions) => currentSessions.filter((session) => session.id !== id))
        } catch (error) {
            setErrorMessage(toFriendlyMessage(error, 'Unable to delete focus session.'))
        } finally {
            setIsDeletingId(null)
        }
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-xl">Your sessions</CardTitle>
                <CardDescription>
                    {sessions.length} recorded {sessions.length === 1 ? 'session' : 'sessions'}
                </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
                {errorMessage ? (
                    <p className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                        {errorMessage}
                    </p>
                ) : null}

                {isLoading ? (
                    <p className="text-sm text-muted-foreground">Loading sessions...</p>
                ) : sessions.length === 0 ? (
                    <p className="text-sm text-muted-foreground">
                        No sessions yet. Complete a focus session to start your history.
                    </p>
                ) : (
                    <ul className="space-y-3">
                        {sessions.map((session) => (
                            <li
                                key={session.id}
                                className="rounded-md border border-border p-4 space-y-2"
                            >
                                <div className="flex items-start justify-between gap-3">
                                    <div className="flex items-center gap-2">
                                        {session.score != null && (
                                            <span className="text-2xl" aria-hidden="true">
                                                {getScoreEmoji(session.score)}
                                            </span>
                                        )}
                                        <div>
                                            <p className="text-sm font-medium text-foreground">
                                                {session.minutes} minute focus session
                                            </p>
                                            <p className="text-xs text-muted-foreground">
                                                {formatDateTime(session.endedAt ?? session.entryDate)}
                                            </p>
                                            <div className="mt-1">
                                                <EnergyBadge energyRating={session.energyRating} />
                                            </div>
                                        </div>
                                    </div>
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => handleDelete(session.id)}
                                        disabled={isDeletingId === session.id}
                                    >
                                        {isDeletingId === session.id ? 'Deleting...' : 'Delete'}
                                    </Button>
                                </div>

                                {session.notes ? (
                                    <p className="text-sm text-foreground whitespace-pre-wrap">
                                        {session.notes}
                                    </p>
                                ) : null}
                            </li>
                        ))}
                    </ul>
                )}
            </CardContent>
        </Card>
    )
}
