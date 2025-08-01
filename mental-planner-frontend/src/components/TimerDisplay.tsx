import React from "react";

interface TimerDisplayProps {
    timeLeft: number;
    totalTime: number;
    formatTime: (seconds: number) => string;
    getColorClass: () => string;
    isRunning: boolean;
}

const radius = 70;
const circumference = 2 * Math.PI * radius;

export function TimerDisplay({
                                 timeLeft,
                                 totalTime,
                                 formatTime,
                                 getColorClass,
                                 isRunning,
                             }: TimerDisplayProps) {
    const progress = 1 - timeLeft / totalTime;
    const strokeDashoffset = circumference * (1 - progress);

    return (
        <div className="relative h-60 w-60 flex items-center justify-center">
            <svg className="absolute w-full h-full" viewBox="0 0 160 160">
                <circle
                    cx="80"
                    cy="80"
                    r={radius}
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="6"
                    className="text-slate-200 dark:text-slate-800"
                />
            </svg>

            <svg className="absolute w-full h-full -rotate-90" viewBox="0 0 160 160">
                <circle
                    cx="80"
                    cy="80"
                    r={radius}
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="6"
                    strokeDasharray={circumference}
                    strokeDashoffset={strokeDashoffset}
                    strokeLinecap="round"
                    className={getColorClass()}
                />
            </svg>

            <div className="flex flex-col items-center">
                <div className="text-5xl font-mono font-semibold tabular-nums">
                    {formatTime(timeLeft)}
                </div>
                {!isRunning && timeLeft < totalTime && (
                    <div className="text-sm mt-2 text-slate-500 dark:text-slate-400">
                        {Math.round(progress * 100)}% complete
                    </div>
                )}
            </div>
        </div>
    );
}
