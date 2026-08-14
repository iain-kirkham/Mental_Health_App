'use client'

import React, { useEffect, useState } from 'react'
import { useAuth } from '@clerk/nextjs'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { getPomodoroSessions, deletePomodoroSession } from '@/lib/pomodoro-api'
import type { PomodoroSessionResponseDTO } from '@/types'

const SCORE_EMOJIS = ['😢', '😕', '😐', '😊', '🎉']

function getScoreEmoji(score: number) {
    return SCORE_EMOJIS[score - 1] || '😐'
}

function EnergyBadge({ energyRating }: { energyRating: PomodoroSessionResponseDTO['energyRating'] }) {
    if (!energyRating) return null

    const isEnergizing = energyRating === 'ENERGIZING'
    return (
        <span
            className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${
                isEnergizing
                    ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300'
                    : 'bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-300'
            }`}
        >
            {isEnergizing ? '⚡ Energizing' : '🪫 Draining'}
        </span>
    )
}

function formatDateTime(dateTime: string | null): string {
    if (!dateTime) return 'Unknown'
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
