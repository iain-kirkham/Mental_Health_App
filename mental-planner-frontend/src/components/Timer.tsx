"use client";

import React from "react";
import Link from "next/link";
import { History } from "lucide-react";
import { Button } from "@/components/ui/button";
import StatusAlerts from "@/components/StatusAlerts";
import PageHeader from "@/components/PageHeader";
import { TimerDisplay } from "./TimerDisplay";
import { TimerControls } from "./TimerControls";
import { usePomodoroSessionContext } from "@/contexts/PomodoroSessionContext";

export default function Timer() {
    const {
        timeLeft,
        totalTime,
        isRunning,
        inputTime,
        setInputTime,
        startPause,
        reset,
        showAlert,
        submitStatus,
        errorMessage,
    } = usePomodoroSessionContext();

    // setInputTime owns the "what is a valid duration" rule and ignores anything invalid.
    const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) =>
        setInputTime(parseInt(e.target.value));

    return (
        <>
            <PageHeader title="🍅 Pomodoro Timer">
                <Link href="/pomodoro/history">
                    <Button variant="ghost" size="sm">
                        <History className="mr-2 h-4 w-4" />
                        View session history
                    </Button>
                </Link>
            </PageHeader>

            {/* Mobile layout - cardless */}
            <div className="md:hidden w-full">
                <div className="w-full px-3 py-3 flex flex-col items-center">
                    <StatusAlerts submitStatus={submitStatus} errorMessage={errorMessage} showAlert={showAlert} alertMessage={undefined} />

                    {/* Timer area: vertically center within ~40vh so it appears in the middle under the navbar */}
                    <div className="w-full flex flex-col justify-center items-center min-h-[40vh]">
                        <div className="flex justify-center items-center">
                            <TimerDisplay
                                timeLeft={timeLeft}
                                totalTime={totalTime}
                                isRunning={isRunning}
                            />
                        </div>
                    </div>

                    {/* Larger gap between timer and controls */}
                    <div className="w-full mt-8">
                        <TimerControls
                            isRunning={isRunning}
                            onStartPause={startPause}
                            onReset={reset}
                            inputTime={inputTime}
                            onInputChange={handleTimeChange}
                            disabledInput={isRunning}
                        />
                    </div>
                </div>
            </div>

            {/* Desktop layout - flat, vertical, fills space */}
            <div className="hidden md:block w-full px-3 py-3 md:px-4">
                <StatusAlerts submitStatus={submitStatus} errorMessage={errorMessage} showAlert={showAlert} alertMessage={undefined} />

                <div className="space-y-6">
                    {/* Timer Display: give a taller area so the timer sits visually centered on desktop too */}
                    <div className="flex items-center justify-center min-h-[34vh] border-b border-border pb-6 mb-6">
                        <TimerDisplay
                            timeLeft={timeLeft}
                            totalTime={totalTime}
                            isRunning={isRunning}
                        />
                    </div>

                    {/* Controls Section */}
                    <div className="w-full md:max-w-xl mx-auto">
                        <h3 className="text-sm font-semibold text-foreground mb-2 text-center md:text-left">
                            ⚙️ Session Settings
                        </h3>
                        <div className="w-full">
                            <TimerControls
                                isRunning={isRunning}
                                onStartPause={startPause}
                                onReset={reset}
                                inputTime={inputTime}
                                onInputChange={handleTimeChange}
                                disabledInput={isRunning}
                            />
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
