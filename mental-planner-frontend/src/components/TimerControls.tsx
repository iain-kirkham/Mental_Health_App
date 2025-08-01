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
            <div className="flex gap-4">
                <Button
                    onClick={onStartPause}
                    variant="outline"
                    className={`${
                        isRunning
                            ? "border-amber-500 text-amber-600 hover:bg-amber-50 dark:border-amber-700 dark:text-amber-400 dark:hover:bg-amber-950/40"
                            : "border-green-500 text-green-600 hover:bg-green-50 dark:border-green-700 dark:text-green-400 dark:hover:bg-green-950/40"
                    }`}
                >
                    {isRunning ? (
                        <>
                            <Pause className="h-4 w-4 mr-2" />
                            Pause
                        </>
                    ) : (
                        <>
                            <Play className="h-4 w-4 mr-2" />
                            Start
                        </>
                    )}
                </Button>

                <Button
                    onClick={onReset}
                    variant="outline"
                    className="border-red-500 text-red-600 hover:bg-red-50 dark:border-red-700 dark:text-red-400 dark:hover:bg-red-950/40"
                >
                    <RefreshCw className="h-4 w-4 mr-2" />
                    Reset
                </Button>
            </div>

            <div className="w-full mt-4">
                <label className="block text-sm font-medium mb-2 text-slate-700 dark:text-slate-200">
                    Minutes
                </label>
                <Input
                    type="number"
                    min={1}
                    value={inputTime}
                    onChange={onInputChange}
                    disabled={disabledInput}
                    className="w-full"
                />
            </div>
        </>
    );
}
