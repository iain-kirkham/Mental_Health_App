import React from "react";
import { formatTime, getTimerColorClass } from "@/lib/pomodoro-format";

interface TimerDisplayProps {
    timeLeft: number;
    totalTime: number;
    isRunning: boolean;
}

const radius = 70;
const circumference = 2 * Math.PI * radius;

export function TimerDisplay({
                                  timeLeft,
                                  totalTime,
                                  isRunning,
                              }: TimerDisplayProps) {
     const progress = totalTime > 0 ? 1 - timeLeft / totalTime : 0;
     const strokeDashoffset = circumference * (1 - progress);
     const colorClass = getTimerColorClass(timeLeft, totalTime);

     return (
         <div
             className="relative h-64 w-64 flex items-center justify-center"
             role="timer"
             aria-label={`Timer: ${formatTime(timeLeft)} remaining`}
         >
            {/* Outer glow effect - subtle for a minimal look */}
            <div className={`absolute inset-0 rounded-full blur-xs opacity-10 ${colorClass}`} aria-hidden="true" />

             {/* Background circle */}
            <svg
                className="absolute w-full h-full"
                viewBox="0 0 160 160"
                aria-hidden="true"
            >
                 <circle
                     cx="80"
                     cy="80"
                     r={radius}
                     fill="none"
                     stroke="currentColor"
                     strokeWidth="8"
                     className="text-muted opacity-60"
                 />
             </svg>

             {/* Progress circle with animation */}
            <svg
                className="absolute w-full h-full -rotate-90"
                viewBox="0 0 160 160"
                aria-hidden="true"
            >
                 <circle
                     cx="80"
                     cy="80"
                     r={radius}
                     fill="none"
                     stroke="currentColor"
                     strokeWidth="8"
                     strokeDasharray={circumference}
                     strokeDashoffset={strokeDashoffset}
                     strokeLinecap="round"
                     className={`${colorClass} transition-all duration-300 ${isRunning ? 'opacity-100' : 'opacity-80'}`}
                     style={{
                        transition: 'stroke-dashoffset 0.3s ease-in-out'
                     }}
                 />
             </svg>

             {/* Center content */}
             <div className="flex flex-col items-center z-10">
                <div
                    className={`text-6xl font-mono font-bold tabular-nums ${colorClass} transition-colors duration-300`}
                    aria-live="polite"
                >
                    {formatTime(timeLeft)}
                </div>
                 {!isRunning && timeLeft < totalTime && (
                     <div className="text-sm mt-3 text-muted-foreground font-medium bg-muted px-3 py-1 rounded-full animate-in fade-in duration-300">
                         {Math.round(progress * 100)}% complete
                     </div>
                 )}
                 {isRunning && (
                    <div className="text-xs mt-3 text-muted-foreground font-medium">
                        ⏱️ In progress...
                    </div>
                 )}
             </div>
         </div>
     );
 }
