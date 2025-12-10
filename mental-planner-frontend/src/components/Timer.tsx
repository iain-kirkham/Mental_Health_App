"use client";

import React, { useState } from "react";
import { useAuth } from "@clerk/nextjs";
import StatusAlerts from "@/components/StatusAlerts";
import { TimerDisplay } from "./TimerDisplay";
import { TimerControls } from "./TimerControls";
import { SessionSummaryModal } from "./SessionSummaryModal";
import PageHeader from "./PageHeader";
import PageInset from "./PageInset";
import useTimer from "@/hooks/useTimer";
import useSessionManager from "@/hooks/useSessionManager";

export default function Timer() {
    const { getToken } = useAuth();
    const [showAlert, setShowAlert] = useState(false);
    const [sessionStartTime, setSessionStartTime] = useState<Date | null>(null);

    const {
        showSessionForm,
        setShowSessionForm,
        score,
        setScore,
        notes,
        setNotes,
        isSubmitting,
        submitStatus,
        errorMessage,
        handleSaveSession: saveSessionManager,
    } = useSessionManager(getToken);

    // useTimer hook handles timing, input and formatting
    const {
        timeLeft,
        totalTime,
        isRunning,
        inputTime,
        setInputTime,
        startPause,
        reset,
        formatTime,
        getColorClass,
        sessionStartTime: hookSessionStartTime,
    } = useTimer(5, ( ) => {
        setShowAlert(true);
        setShowSessionForm(true);
        setTimeout(() => setShowAlert(false), 5000);
    });

    const handleStartPause = () => {
        // keep a copy in local state for the session payload
        if (!isRunning) setSessionStartTime(new Date());
        startPause();
    };

    const handleReset = () => {
        reset();
        setShowAlert(false);
    };

    const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = parseInt(e.target.value);
        if (!isNaN(value) && value > 0) {
            setInputTime(value);
        }
    };

    const handleSaveSession = async () => {
        const result = await saveSessionManager({ startTime: (hookSessionStartTime ?? sessionStartTime), totalTime, timeLeft });

        if (result.ok) {
            setSessionStartTime(null);
            setInputTime(inputTime);
        }
    };

    return (
        <>
            <PageHeader title={<>🍅 Pomodoro Timer</>} subtitle={<>Focus and track your productivity sessions</>} size="wide" />
            <div className="pt-4 md:pt-6" />

            {/* Mobile layout - cardless */}
            <div className="md:hidden w-full">
                {/* Compact mobile top padding so title sits near the navbar */}
                <div className="w-full p-2 bg-transparent dark:bg-transparent flex flex-col items-center">
                    <StatusAlerts submitStatus={submitStatus} errorMessage={errorMessage} showAlert={showAlert} alertMessage={undefined} />

                    {/* Timer area: vertically center within ~40vh so it appears in the middle under the navbar */}
                    <div className="w-full flex flex-col justify-center items-center min-h-[40vh]">
                        <div className="flex justify-center items-center">
                            <TimerDisplay
                                timeLeft={timeLeft}
                                totalTime={totalTime}
                                formatTime={formatTime}
                                getColorClass={getColorClass}
                                isRunning={isRunning}
                            />
                        </div>
                    </div>

                    {/* Larger gap between timer and controls */}
                    <div className="w-full mt-8">
                        <TimerControls
                            isRunning={isRunning}
                            onStartPause={handleStartPause}
                            onReset={handleReset}
                            inputTime={inputTime}
                            onInputChange={handleTimeChange}
                            disabledInput={isRunning}
                        />
                    </div>
                </div>
            </div>

            {/* Desktop layout - cardless, vertical, fills space */}
            <div className="hidden md:block w-full">
                <PageInset size="wide">
                {/* Slightly larger top padding and spacing for desktop content */}

                <StatusAlerts submitStatus={submitStatus} errorMessage={errorMessage} showAlert={showAlert} alertMessage={undefined} />

                <div className="space-y-6">
                    {/* Timer Display: give a taller area so the timer sits visually centered on desktop too */}
                    <div className="p-2 rounded-md bg-white dark:bg-slate-900 flex items-center justify-center min-h-[34vh] mb-6">
                        <TimerDisplay
                            timeLeft={timeLeft}
                            totalTime={totalTime}
                            formatTime={formatTime}
                            getColorClass={getColorClass}
                            isRunning={isRunning}
                        />
                    </div>

                    {/* Controls Section */}
                    <div className="p-2 rounded-md bg-white dark:bg-slate-900 w-full md:max-w-xl mx-auto">
                        <h3 className="text-sm font-semibold text-slate-700 dark:text-slate-200 mb-2 text-center md:text-left">
                            ⚙️ Session Settings
                        </h3>
                        <div className="w-full">
                            <TimerControls
                                isRunning={isRunning}
                                onStartPause={handleStartPause}
                                onReset={handleReset}
                                inputTime={inputTime}
                                onInputChange={handleTimeChange}
                                disabledInput={isRunning}
                            />
                        </div>
                    </div>
                 </div>
                 </PageInset>
             </div>

            {showSessionForm && (
                  <SessionSummaryModal
                      score={score}
                      notes={notes}
                      onScoreChange={(e) => setScore(Number(e.target.value))}
                      onNotesChange={(e) => setNotes(e.target.value)}
                      onCancel={() => setShowSessionForm(false)}
                      onSave={handleSaveSession}
                      isSubmitting={isSubmitting}
                  />
              )}
         </>
     );
 }
