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
     const progress = totalTime > 0 ? 1 - timeLeft / totalTime : 0;
     const strokeDashoffset = circumference * (1 - progress);

     return (
         <div
             className="relative h-64 w-64 flex items-center justify-center"
             role="timer"
             aria-label={`Timer: ${formatTime(timeLeft)} remaining`}
         >
            {/* Outer glow effect - subtle for a minimal look */}
            <div className={`absolute inset-0 rounded-full blur-sm opacity-10 ${getColorClass()}`} aria-hidden="true" />

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
                     className="text-slate-200 dark:text-slate-800 opacity-30"
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
                     className={`${getColorClass()} transition-all duration-300 ${isRunning ? 'opacity-100' : 'opacity-80'}`}
                     style={{
                        transition: 'stroke-dashoffset 0.3s ease-in-out'
                     }}
                 />
             </svg>

             {/* Center content */}
             <div className="flex flex-col items-center z-10">
                <div
                    className={`text-6xl font-mono font-bold tabular-nums ${getColorClass()} transition-colors duration-300`}
                    aria-live="polite"
                >
                    {formatTime(timeLeft)}
                </div>
                 {!isRunning && timeLeft < totalTime && (
                     <div className="text-sm mt-3 text-slate-500 dark:text-slate-400 font-medium bg-slate-100 dark:bg-slate-800 px-3 py-1 rounded-full animate-in fade-in duration-300">
                         {Math.round(progress * 100)}% complete
                     </div>
                 )}
                 {isRunning && (
                    <div className="text-xs mt-3 text-slate-400 dark:text-slate-500 font-medium">
                        ⏱️ In progress...
                    </div>
                 )}
             </div>
         </div>
     );
 }
