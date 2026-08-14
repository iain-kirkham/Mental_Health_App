"use client";

import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";
import { useAuth } from "@clerk/nextjs";
import usePomodoroSession from "@/hooks/usePomodoroSession";
import type { EnergyRating, PomodoroSessionCreationDTO } from "@/types";

const STORAGE_KEY = "pomodoro-session-state-v1";
const DEFAULT_MINUTES = 5;

const minutesToSeconds = (minutes: number) => Math.max(1, Math.floor(minutes)) * 60;

type PersistedState = {
  inputTime: number;
  totalTime: number;
  endAt: number | null;
  remainingWhenPaused: number;
  sessionStartTime: string | null;
  showSessionForm: boolean;
  score: number;
  notes: string;
  energyRating: EnergyRating | null;
};

type PomodoroSessionContextValue = {
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
  showAlert: boolean;
  showSessionForm: boolean;
  setShowSessionForm: (v: boolean) => void;
  score: number;
  setScore: (n: number) => void;
  notes: string;
  setNotes: (s: string) => void;
  energyRating: EnergyRating | null;
  setEnergyRating: (r: EnergyRating | null) => void;
  isSubmitting: boolean;
  submitStatus: "idle" | "success" | "error";
  errorMessage: string;
  handleSaveSession: () => Promise<void>;
};

const PomodoroSessionContext = createContext<PomodoroSessionContextValue | null>(null);

// Remaining time is derived from a fixed end timestamp rather than decremented
// tick-by-tick, so it stays accurate even when the interval is throttled by a
// backgrounded/inactive browser tab.
function computeRemaining(endAt: number | null, remainingWhenPaused: number) {
  if (endAt === null) return remainingWhenPaused;
  return Math.max(0, Math.round((endAt - Date.now()) / 1000));
}

export function PomodoroSessionProvider({ children }: { children: React.ReactNode }) {
  const { getToken } = useAuth();
  const { isSubmitting, submitStatus, errorMessage, saveSession, setSubmitStatus } = usePomodoroSession(getToken);

  const [inputTime, setInputTimeState] = useState<number>(DEFAULT_MINUTES);
  const [totalTime, setTotalTime] = useState<number>(minutesToSeconds(DEFAULT_MINUTES));
  const [endAt, setEndAt] = useState<number | null>(null);
  const [remainingWhenPaused, setRemainingWhenPaused] = useState<number>(minutesToSeconds(DEFAULT_MINUTES));
  const [timeLeft, setTimeLeft] = useState<number>(minutesToSeconds(DEFAULT_MINUTES));
  const [sessionStartTime, setSessionStartTime] = useState<Date | null>(null);
  const [showAlert, setShowAlert] = useState(false);
  const [showSessionForm, setShowSessionForm] = useState(false);
  const [score, setScore] = useState(3);
  const [notes, setNotes] = useState("");
  const [energyRating, setEnergyRating] = useState<EnergyRating | null>(null);
  const [hydrated, setHydrated] = useState(false);

  const expiredRef = useRef(false);
  const statusTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const isRunning = endAt !== null && timeLeft > 0;

  // Restore an in-progress session (e.g. after a page refresh) on mount. This has to run
  // after mount rather than as a lazy useState initializer, since localStorage isn't
  // available during SSR and reading it eagerly on the client would cause a hydration
  // mismatch against the server-rendered defaults.
  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => {
    try {
      const raw = window.localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed: PersistedState = JSON.parse(raw);
        const restoredTimeLeft = computeRemaining(parsed.endAt, parsed.remainingWhenPaused);
        setInputTimeState(parsed.inputTime);
        setTotalTime(parsed.totalTime);
        setEndAt(parsed.endAt);
        setRemainingWhenPaused(parsed.remainingWhenPaused);
        setSessionStartTime(parsed.sessionStartTime ? new Date(parsed.sessionStartTime) : null);
        setShowSessionForm(parsed.showSessionForm || (parsed.endAt !== null && restoredTimeLeft === 0));
        setScore(parsed.score);
        setNotes(parsed.notes);
        setEnergyRating(parsed.energyRating);
        setTimeLeft(restoredTimeLeft);
        if (parsed.endAt !== null && restoredTimeLeft === 0) {
          expiredRef.current = true;
        }
      }
    } catch {
      // Ignore malformed/unavailable storage and fall back to defaults.
    }
    setHydrated(true);
  }, []);
  /* eslint-enable react-hooks/set-state-in-effect */

  useEffect(() => {
    if (!hydrated) return;
    const state: PersistedState = {
      inputTime,
      totalTime,
      endAt,
      remainingWhenPaused,
      sessionStartTime: sessionStartTime ? sessionStartTime.toISOString() : null,
      showSessionForm,
      score,
      notes,
      energyRating,
    };
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }, [hydrated, inputTime, totalTime, endAt, remainingWhenPaused, sessionStartTime, showSessionForm, score, notes, energyRating]);

  useEffect(() => {
    if (!isRunning) return;

    const tick = () => setTimeLeft(computeRemaining(endAt, remainingWhenPaused));
    tick();

    const interval = window.setInterval(tick, 1000);
    document.addEventListener("visibilitychange", tick);
    window.addEventListener("focus", tick);

    return () => {
      window.clearInterval(interval);
      document.removeEventListener("visibilitychange", tick);
      window.removeEventListener("focus", tick);
    };
  }, [isRunning, endAt, remainingWhenPaused]);

  useEffect(() => {
    if (endAt !== null && timeLeft === 0 && !expiredRef.current) {
      expiredRef.current = true;
      setEndAt(null);
      setRemainingWhenPaused(0);
      setShowAlert(true);
      setShowSessionForm(true);
      setTimeout(() => setShowAlert(false), 5000);
    }
  }, [endAt, timeLeft]);

  const startPause = useCallback(() => {
    if (endAt === null) {
      if (remainingWhenPaused <= 0) return;
      if (!sessionStartTime) setSessionStartTime(new Date());
      expiredRef.current = false;
      setEndAt(Date.now() + remainingWhenPaused * 1000);
    } else {
      const remaining = computeRemaining(endAt, remainingWhenPaused);
      setRemainingWhenPaused(remaining);
      setTimeLeft(remaining);
      setEndAt(null);
    }
  }, [endAt, remainingWhenPaused, sessionStartTime]);

  const reset = useCallback(() => {
    const seconds = minutesToSeconds(inputTime);
    setEndAt(null);
    setRemainingWhenPaused(seconds);
    setTimeLeft(seconds);
    setTotalTime(seconds);
    setSessionStartTime(null);
    setShowAlert(false);
    expiredRef.current = false;
  }, [inputTime]);

  const setInputTime = useCallback((minutes: number) => {
    if (isNaN(minutes) || minutes <= 0) return;
    setInputTimeState(minutes);
    const seconds = minutesToSeconds(minutes);
    setTotalTime(seconds);
    setRemainingWhenPaused(seconds);
    setTimeLeft(seconds);
  }, []);

  const formatTime = useCallback((seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  }, []);

  const getColorClass = useCallback(() => {
    const percentRemaining = totalTime > 0 ? timeLeft / totalTime : 0;
    if (percentRemaining > 0.66) return "text-green-500 dark:text-green-400";
    if (percentRemaining > 0.33) return "text-amber-500 dark:text-amber-400";
    return "text-red-500 dark:text-red-400";
  }, [timeLeft, totalTime]);

  useEffect(() => {
    return () => {
      if (statusTimeoutRef.current) clearTimeout(statusTimeoutRef.current);
    };
  }, []);

  const handleSaveSession = useCallback(async () => {
    const sessionData: PomodoroSessionCreationDTO = {
      startTime: sessionStartTime?.toISOString() ?? null,
      endTime: new Date().toISOString(),
      duration: Math.round((totalTime - timeLeft) / 60),
      score,
      notes: notes.trim(),
      energyRating,
    };

    const result = await saveSession(sessionData);

    if (result.ok) {
      setShowSessionForm(false);
      setScore(3);
      setNotes("");
      setEnergyRating(null);
      setInputTime(inputTime);

      if (statusTimeoutRef.current) clearTimeout(statusTimeoutRef.current);
      statusTimeoutRef.current = setTimeout(() => setSubmitStatus("idle"), 3000);
    }
  }, [sessionStartTime, totalTime, timeLeft, score, notes, energyRating, saveSession, inputTime, setInputTime, setSubmitStatus]);

  const value: PomodoroSessionContextValue = {
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
    showAlert,
    showSessionForm,
    setShowSessionForm,
    score,
    setScore,
    notes,
    setNotes,
    energyRating,
    setEnergyRating,
    isSubmitting,
    submitStatus,
    errorMessage,
    handleSaveSession,
  };

  return <PomodoroSessionContext.Provider value={value}>{children}</PomodoroSessionContext.Provider>;
}

export function usePomodoroSessionContext() {
  const ctx = useContext(PomodoroSessionContext);
  if (!ctx) {
    throw new Error("usePomodoroSessionContext must be used within a PomodoroSessionProvider");
  }
  return ctx;
}
