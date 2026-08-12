import { useEffect, useState } from "react";

type UseTimerResult = {
  timeLeft: number;
  totalTime: number;
  isRunning: boolean;
  inputTime: number;
  setInputTime: (minutes: number) => void;
  startPause: () => void;
  reset: () => void;
  formatTime: (seconds: number) => string;
  getColorClass: () => string;
  sessionStartTime: Date | null;
};

export default function useTimer(initialMinutes = 5, onExpire?: () => void): UseTimerResult {
  const initialSeconds = Math.max(1, Math.floor(initialMinutes)) * 60;
  const [timeLeft, setTimeLeft] = useState<number>(initialSeconds);
  const [internalRunning, setInternalRunning] = useState<boolean>(false);
  const [inputTime, setInputTimeState] = useState<number>(initialMinutes);
  const [totalTime, setTotalTime] = useState<number>(initialSeconds);
  const [sessionStartTime, setSessionStartTime] = useState<Date | null>(null);

  // Running is only meaningful while time remains; once it hits zero the timer has expired.
  const isRunning = internalRunning && timeLeft > 0;

  // Keep totalTime in sync if initialMinutes changes (rare)
  const [prevInitialMinutes, setPrevInitialMinutes] = useState(initialMinutes);
  if (initialMinutes !== prevInitialMinutes) {
    const secs = Math.max(1, Math.floor(initialMinutes)) * 60;
    setPrevInitialMinutes(initialMinutes);
    setTimeLeft(secs);
    setTotalTime(secs);
    setInputTimeState(initialMinutes);
  }

  useEffect(() => {
    if (!isRunning) {
      return;
    }

    const interval = window.setInterval(() => {
      setTimeLeft((prev) => Math.max(0, prev - 1));
    }, 1000);

    return () => clearInterval(interval);
  }, [isRunning]);

  // Signal expiry as a side effect once the countdown actually reaches zero
  useEffect(() => {
    if (internalRunning && timeLeft === 0 && typeof onExpire === "function") {
      onExpire();
    }
  }, [internalRunning, timeLeft, onExpire]);

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  const getColorClass = () => {
    const percentRemaining = totalTime > 0 ? timeLeft / totalTime : 0;
    if (percentRemaining > 0.66) return "text-green-500 dark:text-green-400";
    if (percentRemaining > 0.33) return "text-amber-500 dark:text-amber-400";
    return "text-red-500 dark:text-red-400";
  };

  const startPause = () => {
    if (!internalRunning) {
      setSessionStartTime(new Date());
    }
    setInternalRunning((s) => !s);
  };

  const reset = () => {
    const newTimeInSeconds = Math.max(1, Math.floor(inputTime)) * 60;
    setTimeLeft(newTimeInSeconds);
    setTotalTime(newTimeInSeconds);
    setInternalRunning(false);
  };

  const setInputTime = (minutes: number) => {
    if (!isNaN(minutes) && minutes > 0) {
      setInputTimeState(minutes);
      const newTimeInSeconds = Math.max(1, Math.floor(minutes)) * 60;
      setTimeLeft(newTimeInSeconds);
      setTotalTime(newTimeInSeconds);
    }
  };

  return {
    timeLeft,
    totalTime,
    isRunning,
    inputTime,
    setInputTime,
    startPause,
    reset,
    formatTime,
    getColorClass,
    sessionStartTime,
  } as UseTimerResult;
}

