import { useEffect, useRef, useState } from "react";

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
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [inputTime, setInputTimeState] = useState<number>(initialMinutes);
  const [totalTime, setTotalTime] = useState<number>(initialSeconds);
  const sessionStartTimeRef = useRef<Date | null>(null);

  // Keep totalTime in sync if initialMinutes changes (rare)
  useEffect(() => {
    const secs = Math.max(1, Math.floor(initialMinutes)) * 60;
    setTimeLeft(secs);
    setTotalTime(secs);
    setInputTimeState(initialMinutes);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialMinutes]);

  useEffect(() => {
    let interval: number | undefined;

    if (isRunning && timeLeft > 0) {
      interval = window.setInterval(() => {
        setTimeLeft((prev) => prev - 1);
      }, 1000);
    } else if (isRunning && timeLeft === 0) {
      // stop and signal expiry
      setIsRunning(false);
      if (typeof onExpire === "function") onExpire();
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isRunning, timeLeft, onExpire]);

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
    if (!isRunning) {
      sessionStartTimeRef.current = new Date();
    }
    setIsRunning((s) => !s);
  };

  const reset = () => {
    if (isRunning || (!isRunning && timeLeft < totalTime)) {
      // signal expiry-like behaviour by calling onExpire
      if (typeof onExpire === "function") onExpire();
    }
    const newTimeInSeconds = Math.max(1, Math.floor(inputTime)) * 60;
    setTimeLeft(newTimeInSeconds);
    setTotalTime(newTimeInSeconds);
    setIsRunning(false);
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
    sessionStartTime: sessionStartTimeRef.current,
  } as UseTimerResult;
}

