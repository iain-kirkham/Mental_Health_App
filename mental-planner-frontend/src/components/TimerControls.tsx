import React from "react";
import { Button } from "@/components/ui/button";
import { Play, Pause, RefreshCw } from "lucide-react";
import { Input } from "@/components/ui/input";

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
                <div className="flex gap-2 md:gap-4 w-full" role="group" aria-label="Timer controls">
                    <Button
                        onClick={onStartPause}
                        variant={isRunning ? "outline" : "default"}
                        size="lg"
                        className={`flex-1 font-medium text-sm md:text-base py-2 md:py-3 transition-colors duration-200 ${
                            isRunning ? "border-status-active/50 text-status-active hover:bg-status-active/10" : ""
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
                        className="flex-1 font-medium border-border text-muted-foreground hover:border-destructive/40 hover:text-destructive hover:bg-destructive/5 transition-colors duration-200"
                        aria-label="Reset timer"
                    >
                        <RefreshCw className="h-5 w-5 mr-2" aria-hidden="true" />
                        Reset
                    </Button>
                  </div>
            </div>

            <div className="w-full mt-4 md:mt-6 border-t border-border pt-3 md:pt-4">
                <label
                    htmlFor="timer-minutes-input"
                    className="block text-sm font-semibold mb-3 text-foreground"
                >
                    Session length (minutes)
                </label>
                <Input
                    id="timer-minutes-input"
                    type="number"
                    min={1}
                    max={120}
                    value={inputTime}
                    onChange={onInputChange}
                    disabled={disabledInput}
                    className="w-full text-lg font-semibold text-center shadow-xs"
                    aria-label="Timer duration in minutes"
                />
                {disabledInput && (
                    <p className="text-xs text-muted-foreground mt-2 text-center">
                        Pause the timer to change duration
                    </p>
                )}
            </div>
         </>
     );
 }
