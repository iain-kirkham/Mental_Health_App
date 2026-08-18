'use client'

import React, { useState, useEffect, useMemo, useRef, useCallback } from 'react';
import { useAuth } from "@clerk/nextjs";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { BookOpen } from "lucide-react";
import { API_ENDPOINTS, authenticatedFetch } from "@/lib/api-config";
import { toFriendlyMessage } from "@/lib/connectivity";
import type { MoodEntryCreationDTO } from "@/types";
import type { FormErrors } from "@/components/mood/types";
import MoodForm from "@/components/mood/MoodForm";
import PageHeader from "@/components/PageHeader";

/** Maximum length for a custom factor name. */
const MAX_FACTOR_LENGTH = 50;

function formatDate(date: Date | undefined): string {
    if (!date || isNaN(date.getTime())) {
        return "Pick a date";
    }
    const options: Intl.DateTimeFormatOptions = { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' };
    return date.toLocaleDateString('en-GB', options);
}

function formatTime(date: Date | undefined): string {
    if (!date || isNaN(date.getTime())) {
        return "--:--";
    }
    return date.toTimeString().slice(0, 5);
}

export default function MoodTracker() {
    const { getToken } = useAuth();
    const [date, setDate] = useState<Date | undefined>(new Date());
    const [time, setTime] = useState<string>(formatTime(new Date()));
    const [selectedMood, setSelectedMood] = useState<number | null>(null);
    const [notes, setNotes] = useState<string>("");
    const [factors, setFactors] = useState<string[]>([]);
    const [newFactor, setNewFactor] = useState<string>('');
    const [showFactorInput, setShowFactorInput] = useState<boolean>(false);
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
    const [submitStatus, setSubmitStatus] = useState<'idle' | 'success' | 'error'>('idle');
    const [errorMessage, setErrorMessage] = useState<string>('');

    // Ref to clear the auto-hide timeout on unmount
    const successTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    // Clean up timeout on unmount
    useEffect(() => {
        return () => {
            if (successTimeoutRef.current) clearTimeout(successTimeoutRef.current);
        };
    }, []);

    const validateForm = useCallback((): FormErrors => {
        const errors: FormErrors = {};
        if (selectedMood === null) errors.mood = 'How were you feeling? Tap an emoji to choose.';
        if (!date || isNaN(date.getTime())) errors.date = 'Please pick a date using the calendar.';
        if (!/^([01]\d|2[0-3]):[0-5]\d$/.test(time)) errors.time = 'Use 24-hour time, e.g. 14:30.';
        if (newFactor && newFactor.trim().length > MAX_FACTOR_LENGTH) errors.newFactor = `Keep custom factors under ${MAX_FACTOR_LENGTH} characters.`;
        return errors;
    }, [selectedMood, date, time, newFactor]);

    // Kept updated as user changes input
    const formErrors = useMemo(() => validateForm(), [validateForm]);

    // Event handlers
    const handleSubmit = async () => {
        // Validation
        const errors = validateForm();
        if (Object.keys(errors).length > 0) {
            setSubmitStatus('error');
            setErrorMessage('Some fields need attention — please correct them and try again.');
            return;
        }

        setIsSubmitting(true);
        setSubmitStatus('idle');
        setErrorMessage('');

        try {
            // Construct dateTime with validation
            let dateTime: string;

            if (date && !isNaN(date.getTime()) && time) {
                const [hours, minutes] = time.split(':').map(Number);

                if (isNaN(hours) || isNaN(minutes) || hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                    dateTime = new Date().toISOString();
                } else {
                    const dateObj = new Date(date);
                    dateObj.setHours(hours);
                    dateObj.setMinutes(minutes);
                    dateObj.setSeconds(0);
                    dateObj.setMilliseconds(0);
                    dateTime = dateObj.toISOString();
                }
            } else {
                dateTime = new Date().toISOString();
            }

            const moodEntry: MoodEntryCreationDTO = {
                moodScore: selectedMood!,
                dateTime: dateTime,
                factors: factors,
                notes: notes.trim()
            };

            const response = await authenticatedFetch(
                API_ENDPOINTS.mood,
                {
                    method: 'POST',
                    body: JSON.stringify(moodEntry)
                },
                getToken
            );

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                const message = errorData.message || `We couldn't save your mood entry right now. Please try again.`;
                setSubmitStatus('error');
                setErrorMessage(message);
            } else {
                await response.json();

                // Success - reset form
                setSubmitStatus('success');
                setSelectedMood(null);
                setNotes("");
                setFactors([]);
                setDate(new Date());
                setTime(formatTime(new Date()));

                // Auto-hide success message after 3 seconds
                if (successTimeoutRef.current) clearTimeout(successTimeoutRef.current);
                successTimeoutRef.current = setTimeout(() => {
                    setSubmitStatus('idle');
                }, 3000);
            }

        } catch (error) {
            setSubmitStatus('error');
            setErrorMessage(toFriendlyMessage(error, "We couldn't save your entry right now. Please try again."));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <>
            <PageHeader title="💭 How are you feeling?">
                <Link href="/mood-tracker/history">
                    <Button variant="ghost" size="sm">
                        <BookOpen className="mr-2 h-4 w-4" />
                        View journal history
                    </Button>
                </Link>
            </PageHeader>

            <MoodForm
                submitStatus={submitStatus}
                errorMessage={errorMessage}
                isSubmitting={isSubmitting}
                selectedMood={selectedMood}
                setSelectedMood={setSelectedMood}
                formErrors={formErrors}
                date={date}
                setDate={setDate}
                time={time}
                setTime={setTime}
                factors={factors}
                setFactors={setFactors}
                newFactor={newFactor}
                setNewFactor={setNewFactor}
                showFactorInput={showFactorInput}
                setShowFactorInput={setShowFactorInput}
                notes={notes}
                setNotes={setNotes}
                handleSubmit={handleSubmit}
                formatDate={formatDate}
            />
        </>
    );
}