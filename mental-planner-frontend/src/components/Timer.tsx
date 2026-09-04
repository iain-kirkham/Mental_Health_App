"use client";

import React from "react";
import Link from "next/link";
import { format } from "date-fns";
import { useAuth } from "@clerk/nextjs";
import { useQuery } from "@tanstack/react-query";
import { History } from "lucide-react";
import { Button } from "@/components/ui/button";
import StatusAlerts from "@/components/StatusAlerts";
import PageHeader from "@/components/PageHeader";
import { TimerDisplay } from "./TimerDisplay";
import { TimerControls } from "./TimerControls";
import { usePomodoroSessionContext } from "@/contexts/PomodoroSessionContext";
import { useTimerStore } from "@/store/timerStore";
import { getTasksForDate } from "@/lib/tasks-api";

const DEFAULT_MINUTES = 5;

export default function Timer() {
    const { submitStatus, errorMessage } = usePomodoroSessionContext();
    const { getToken } = useAuth();

    const mode = useTimerStore((state) => state.mode);
    const storeIsRunning = useTimerStore((state) => state.isRunning);
    const elapsedSeconds = useTimerStore((state) => state.elapsedSeconds);
    const sessionLengthMinutes = useTimerStore((state) => state.sessionLengthMinutes);
    const activeTaskId = useTimerStore((state) => state.activeTaskId);
    const startTimer = useTimerStore((state) => state.startTimer);
    const pauseTimer = useTimerStore((state) => state.pauseTimer);
    const cancelTimer = useTimerStore((state) => state.cancelTimer);

    const [inputTime, setInputTimeState] = React.useState(DEFAULT_MINUTES);
    const [selectedTaskId, setSelectedTaskId] = React.useState<number | null>(null);

    const todayKey = format(new Date(), "yyyy-MM-dd");
    const { data: todaysTasks = [], isLoading: isLoadingTasks } = useQuery({
        queryKey: ["tasks", todayKey],
        queryFn: () => getTasksForDate(todayKey, getToken),
    });

    // A countdown session in progress (running or paused mid-session) elsewhere overrides the
    // uncommitted local duration/task choice below - this page reflects whatever's actually
    // accumulating in the shared timer, not a second, disconnected "what I meant to start" state.
    const hasCountdownProgress = mode === "countdown" && sessionLengthMinutes !== null;
    const isRunning = storeIsRunning && mode === "countdown";
    const totalTime = hasCountdownProgress ? sessionLengthMinutes * 60 : inputTime * 60;
    const timeLeft = hasCountdownProgress ? Math.max(0, totalTime - elapsedSeconds) : inputTime * 60;
    const showAlert = false; // countdown-expiry alert now happens via the reflection form opening itself

    const setInputTime = (minutes: number) => {
        if (isNaN(minutes) || minutes <= 0) return;
        setInputTimeState(minutes);
    };

    const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) =>
        setInputTime(parseInt(e.target.value));

    const startPause = () => {
        if (isRunning) {
            pauseTimer();
            return;
        }
        const taskId = hasCountdownProgress ? activeTaskId : selectedTaskId;
        const currentActualMinutes = taskId
            ? (todaysTasks.find((t) => t.id === taskId)?.actualMinutes ?? 0)
            : 0;
        startTimer(taskId, currentActualMinutes, { mode: "countdown", sessionLengthMinutes: inputTime });
    };

    const reset = () => {
        cancelTimer();
        setSelectedTaskId(null);
    };

    return (
        <>
            <PageHeader title="Focus timer">
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
                        <TaskLinkPicker
                            tasks={todaysTasks}
                            selectedTaskId={hasCountdownProgress ? activeTaskId : selectedTaskId}
                            onChange={setSelectedTaskId}
                            disabled={hasCountdownProgress}
                            isLoading={isLoadingTasks}
                        />
                        <TimerControls
                            isRunning={isRunning}
                            onStartPause={startPause}
                            onReset={reset}
                            inputTime={inputTime}
                            onInputChange={handleTimeChange}
                            disabledInput={hasCountdownProgress}
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
                            Session settings
                        </h3>
                        <div className="w-full space-y-4">
                            <TaskLinkPicker
                                tasks={todaysTasks}
                                selectedTaskId={hasCountdownProgress ? activeTaskId : selectedTaskId}
                                onChange={setSelectedTaskId}
                                disabled={hasCountdownProgress}
                                isLoading={isLoadingTasks}
                            />
                            <TimerControls
                                isRunning={isRunning}
                                onStartPause={startPause}
                                onReset={reset}
                                inputTime={inputTime}
                                onInputChange={handleTimeChange}
                                disabledInput={hasCountdownProgress}
                            />
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

function TaskLinkPicker({
    tasks,
    selectedTaskId,
    onChange,
    disabled,
    isLoading,
}: {
    tasks: { id: number; title: string }[];
    selectedTaskId: number | null;
    onChange: (id: number | null) => void;
    disabled: boolean;
    isLoading?: boolean;
}) {
    return (
        <div>
            <label htmlFor="pomodoro-task-link" className="block text-sm font-semibold mb-2 text-foreground">
                Track time against (optional)
            </label>
            <select
                id="pomodoro-task-link"
                value={selectedTaskId ?? ""}
                onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
                disabled={disabled || isLoading}
                className="w-full h-10 rounded-md border-2 border-input bg-transparent px-3 text-sm shadow-xs disabled:opacity-50 disabled:cursor-not-allowed"
                aria-label="Link this session to a task"
            >
                {isLoading ? (
                    <option value="">Loading today&apos;s tasks…</option>
                ) : (
                    <>
                        <option value="">No task - just focus</option>
                        {tasks.map((task) => (
                            <option key={task.id} value={task.id}>
                                {task.title || "Untitled task"}
                            </option>
                        ))}
                    </>
                )}
            </select>
            {disabled && (
                <p className="text-xs text-muted-foreground mt-2">
                    Cancel the current session to change which task this is linked to.
                </p>
            )}
        </div>
    );
}
