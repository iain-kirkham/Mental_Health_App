'use client'

import React, { useEffect, useState } from 'react'
import { useAuth } from '@clerk/nextjs'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { getPomodoroSessions, deletePomodoroSession } from '@/lib/pomodoro-api'
import { getScoreEmoji, getEnergyRatingOption } from '@/lib/pomodoro-format'
import { formatDateTime } from '@/lib/utils'
import type { PomodoroSessionResponseDTO } from '@/types'

function EnergyBadge({ energyRating }: { energyRating: PomodoroSessionResponseDTO['energyRating'] }) {
    const option = getEnergyRatingOption(energyRating)
    if (!option) return null

    return (
        <Badge variant="outline" className={option.badgeClass}>
            {option.icon} {option.label}
        </Badge>
    )
}

export default function PomodoroHistory() {
    const { getToken } = useAuth()
    const [sessions, setSessions] = useState<PomodoroSessionResponseDTO[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [errorMessage, setErrorMessage] = useState('')
    const [isDeletingId, setIsDeletingId] = useState<number | null>(null)

    useEffect(() => {
        const loadSessions = async () => {
            setIsLoading(true)
            setErrorMessage('')
            try {
                const loadedSessions = await getPomodoroSessions(getToken)
                const sorted = [...loadedSessions].sort(
                    (a, b) => new Date(b.endTime).getTime() - new Date(a.endTime).getTime()
                )
                setSessions(sorted)
            } catch (error) {
                const message = error instanceof Error ? error.message : 'Unable to load pomodoro sessions.'
                setErrorMessage(message)
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
            await deletePomodoroSession(id, getToken)
            setSessions((currentSessions) => currentSessions.filter((session) => session.id !== id))
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Unable to delete pomodoro session.'
            setErrorMessage(message)
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
                    <p className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-800 dark:border-rose-900 dark:bg-rose-950 dark:text-rose-200">
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
                                        <span className="text-2xl" aria-hidden="true">
                                            {getScoreEmoji(session.score)}
                                        </span>
                                        <div>
                                            <p className="text-sm font-medium text-foreground">
                                                {session.duration} minute focus session
                                            </p>
                                            <p className="text-xs text-muted-foreground">
                                                {formatDateTime(session.endTime)}
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
