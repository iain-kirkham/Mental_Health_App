"use client";

import React from "react";
import Link from "next/link";
import { History } from "lucide-react";
import { Button } from "@/components/ui/button";
import StatusAlerts from "@/components/StatusAlerts";
import { TimerDisplay } from "./TimerDisplay";
import { TimerControls } from "./TimerControls";
import PageHeader from "./PageHeader";
import PageInset from "./PageInset";
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
        formatTime,
        getColorClass,
        showAlert,
        submitStatus,
        errorMessage,
    } = usePomodoroSessionContext();

    const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = parseInt(e.target.value);
        if (!isNaN(value) && value > 0) {
            setInputTime(value);
        }
    };

    return (
        <>
            <PageHeader title={<>🍅 Pomodoro Timer</>} subtitle={<>Focus and track your productivity sessions</>} size="wide" />
            <div className="pt-4 md:pt-6" />

            <PageInset size="wide" className="flex justify-end pb-2">
                <Link href="/pomodoro/history">
                    <Button variant="outline" size="sm">
                        <History className="mr-2 h-4 w-4" />
                        View session history
                    </Button>
                </Link>
            </PageInset>

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
                            onStartPause={startPause}
                            onReset={reset}
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
                    <div className="p-2 rounded-md bg-card flex items-center justify-center min-h-[34vh] mb-6">
                        <TimerDisplay
                            timeLeft={timeLeft}
                            totalTime={totalTime}
                            formatTime={formatTime}
                            getColorClass={getColorClass}
                            isRunning={isRunning}
                        />
                    </div>

                    {/* Controls Section */}
                    <div className="p-2 rounded-md bg-card w-full md:max-w-xl mx-auto">
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
                 </PageInset>
             </div>
         </>
     );
 }
