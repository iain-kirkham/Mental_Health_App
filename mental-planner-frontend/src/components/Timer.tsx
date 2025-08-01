"use client";

import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { TimerDisplay } from "./TimerDisplay";
import { TimerControls } from "./TimerControls";
import { SessionSummaryModal } from "./SessionSummaryModal";

export default function Timer() {
    const [timeLeft, setTimeLeft] = useState(300);
    const [isRunning, setIsRunning] = useState(false);
    const [inputTime, setInputTime] = useState(5);
    const [showAlert, setShowAlert] = useState(false);
    const [totalTime, setTotalTime] = useState(300);
    const [showSessionForm, setShowSessionForm] = useState(false);
    const [score, setScore] = useState(3);
    const [notes, setNotes] = useState("");
    const [sessionStartTime, setSessionStartTime] = useState<Date | null>(null);

    useEffect(() => {
        let interval: number | null = null;
        if (isRunning && timeLeft > 0) {
            interval = window.setInterval(() => {
                setTimeLeft((prev) => prev - 1);
            }, 1000);
        } else if (isRunning && timeLeft === 0) {
            setIsRunning(false);
            setShowAlert(true);
            setShowSessionForm(true);
            setTimeout(() => setShowAlert(false), 5000);
        }
        return () => {
            if (interval !== null) {
                clearInterval(interval);
            }
        };
    }, [isRunning, timeLeft]);

    const formatTime = (seconds: number) => {
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${minutes.toString().padStart(2, "0")}:${secs
            .toString()
            .padStart(2, "0")}`;
    };

    const getColorClass = () => {
        const percentRemaining = timeLeft / totalTime;
        if (percentRemaining > 0.66) return "text-green-500 dark:text-green-400";
        if (percentRemaining > 0.33) return "text-amber-500 dark:text-amber-400";
        return "text-red-500 dark:text-red-400";
    };

    const handleStartPause = () => {
        if (!isRunning) {
            setSessionStartTime(new Date());
        }
        setIsRunning(!isRunning);
    };

    const handleReset = () => {
        // Show modal if timer is running OR if timer was started and paused (timeLeft < totalTime)
        if (isRunning || (!isRunning && timeLeft < totalTime)) {
            setIsRunning(false);
            setShowSessionForm(true);
        }

        const newTimeInSeconds = inputTime * 60;
        setTimeLeft(newTimeInSeconds);
        setTotalTime(newTimeInSeconds);
        setShowAlert(false);
    };


    const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = parseInt(e.target.value);
        if (!isNaN(value) && value > 0) {
            setInputTime(value);
            const newTimeInSeconds : number = value * 60;
            setTimeLeft(newTimeInSeconds);
            setTotalTime(newTimeInSeconds);
        }
    };

    const handleSaveSession = async () => {
        const sessionData = {
            startTime: sessionStartTime?.toISOString() ?? null,
            endTime: new Date().toISOString(),
            duration: Math.round((totalTime - timeLeft) / 60), // duration in seconds to match your entity
            score,
            notes,
        };

        console.log("Session data to save:", sessionData);

        try {
            const response = await fetch('http://localhost:8080/api/pomodoro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(sessionData),
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const savedSession = await response.json();
            console.log('Session saved successfully:', savedSession);

            // Reset form and timer after a successful save
            setShowSessionForm(false);
            setScore(3);
            setNotes("");
            setSessionStartTime(null);
            const newTimeInSeconds = inputTime * 60;
            setTimeLeft(newTimeInSeconds);
            setTotalTime(newTimeInSeconds);

        } catch (error) {
            console.error('Error saving session:', error);
            // You might want to show an error message to the user here
            alert('Failed to save session. Please try again.');
        }
    };


    return (
        <>
            <Card className="max-w-md mx-auto">
                <CardHeader>
                    <CardTitle className="text-center">Timer</CardTitle>
                </CardHeader>
                <CardContent className="flex flex-col items-center space-y-6">
                    {showAlert && (
                        <div className="w-full bg-red-100 text-red-700 p-2 rounded flex items-center space-x-2">
              <span>
                <strong>Time's up!</strong>
              </span>
                        </div>
                    )}

                    <TimerDisplay
                        timeLeft={timeLeft}
                        totalTime={totalTime}
                        formatTime={formatTime}
                        getColorClass={getColorClass}
                        isRunning={isRunning}
                    />

                    <TimerControls
                        isRunning={isRunning}
                        onStartPause={handleStartPause}
                        onReset={handleReset}
                        inputTime={inputTime}
                        onInputChange={handleTimeChange}
                        disabledInput={isRunning}
                    />
                </CardContent>
            </Card>

            {showSessionForm && (
                <SessionSummaryModal
                    score={score}
                    notes={notes}
                    onScoreChange={(e) => setScore(Number(e.target.value))}
                    onNotesChange={(e) => setNotes(e.target.value)}
                    onCancel={() => setShowSessionForm(false)}
                    onSave={handleSaveSession}
                />
            )}
        </>
    );
}
