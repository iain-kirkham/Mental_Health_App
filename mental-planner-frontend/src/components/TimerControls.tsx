import React from "react";
import { Button } from "@/components/ui/button";
import { Play, Pause, RefreshCw } from "lucide-react";
import { Input } from "@/components/ui/input";
import PageInset from "@/components/PageInset";

interface TimerControlsProps {
    isRunning: boolean;
    onStartPause: () => void;
    onReset: () => void;
    inputTime: number;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    disabledInput: boolean;
}

export function TimerControls({
                                  isRunning,
                                  onStartPause,
                                  onReset,
                                  inputTime,
                                  onInputChange,
                                  disabledInput,
                              }: TimerControlsProps) {
    return (
        <>
            <div className="w-full">
                <PageInset size="md" className="px-8 md:px-0">
                  <div className="flex gap-2 md:gap-4 w-full" role="group" aria-label="Timer controls">
                    <Button
                        onClick={onStartPause}
                        variant="outline"
                        size="lg"
                        className={`flex-1 font-semibold text-sm md:text-base py-2 md:py-3 transition-all duration-300 shadow-md hover:shadow-lg ${
                            isRunning
                                ? "border-2 border-amber-500 text-amber-600 hover:bg-amber-50 dark:border-amber-600 dark:text-amber-400 dark:hover:bg-amber-950/40 bg-amber-50/50 dark:bg-amber-950/20"
                                : "border-2 border-green-500 text-green-600 hover:bg-green-50 dark:border-green-600 dark:text-green-400 dark:hover:bg-green-950/40 bg-green-50/50 dark:bg-green-950/20"
                        }`}
                        aria-label={isRunning ? "Pause timer" : "Start timer"}
                    >
                        {isRunning ? (
                            <>
                                <Pause className="h-5 w-5 mr-2" aria-hidden="true" />
                                Pause
                            </>
                        ) : (
                            <>
                                <Play className="h-5 w-5 mr-2" aria-hidden="true" />
                                Start
                            </>
                        )}
                    </Button>

                    <Button
                        onClick={onReset}
                        variant="outline"
                        size="lg"
                        className="flex-1 font-semibold border-2 border-red-500 text-red-600 hover:bg-red-50 dark:border-red-600 dark:text-red-400 dark:hover:bg-red-950/40 bg-red-50/50 dark:bg-red-950/20 transition-all duration-300 shadow-md hover:shadow-lg"
                        aria-label="Reset timer"
                    >
                        <RefreshCw className="h-5 w-5 mr-2" aria-hidden="true" />
                        Reset
                    </Button>
                  </div>
                </PageInset>
            </div>

            <PageInset size="md" className="px-8 md:px-0">
              <div className="w-full mt-4 md:mt-6 bg-slate-50 dark:bg-slate-900/50 p-3 md:p-4 rounded-md md:rounded-lg border border-slate-200 dark:border-slate-800">
                <label
                    htmlFor="timer-minutes-input"
                    className="block text-sm font-semibold mb-3 text-slate-700 dark:text-slate-200"
                >
                    ⏲️ Set Duration (Minutes)
                </label>
                <Input
                    id="timer-minutes-input"
                    type="number"
                    min={1}
                    max={120}
                    value={inputTime}
                    onChange={onInputChange}
                    disabled={disabledInput}
                    className="w-full text-lg font-semibold text-center shadow-sm"
                    aria-label="Timer duration in minutes"
                />
                {disabledInput && (
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-2 text-center">
                        Pause the timer to change duration
                    </p>
                )}
              </div>
            </PageInset>
         </>
     );
 }
