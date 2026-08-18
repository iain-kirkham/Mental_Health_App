"use client";

import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { useAuth } from "@clerk/nextjs";
import usePomodoroSession from "@/hooks/usePomodoroSession";
import { useTimerStore } from "@/store/timerStore";
import type { EnergyRating, PomodoroSessionCreationDTO } from "@/types";

type PomodoroSessionContextValue = {
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

// Countdown time-tracking itself lives in useTimerStore (shared with the plain task stopwatch,
// so a task's tracked time has exactly one writer). This context owns only what's left once a
// countdown session completes: the score/energy/notes reflection form, and saving it as a
// PomodoroSession journal entry.
export function PomodoroSessionProvider({ children }: { children: React.ReactNode }) {
  const { getToken } = useAuth();
  const { isSubmitting, submitStatus, errorMessage, saveSession, setSubmitStatus } = usePomodoroSession(getToken);

  const [showSessionForm, setShowSessionForm] = useState(false);
  const [score, setScore] = useState(3);
  const [notes, setNotes] = useState("");
  const [energyRating, setEnergyRating] = useState<EnergyRating | null>(null);

  // Captured from timerStore at the moment a countdown session completes, since timerStore
  // clears its own session fields as part of that same completion - by the time the user fills
  // in and saves the reflection form, this ref is the only place that data is still available.
  const completedSessionRef = useRef<{ taskId: number | null; durationMinutes: number; startedAt: string | null } | null>(null);
  const statusTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return useTimerStore.subscribe((state, prevState) => {
      if (state.lastCompletedSession && state.lastCompletedSession !== prevState.lastCompletedSession) {
        completedSessionRef.current = state.lastCompletedSession;
        setShowSessionForm(true);
      }
    });
  }, []);

  useEffect(() => {
    return () => {
      if (statusTimeoutRef.current) clearTimeout(statusTimeoutRef.current);
    };
  }, []);

  const handleSaveSession = useCallback(async () => {
    const completed = completedSessionRef.current;
    const sessionData: PomodoroSessionCreationDTO = {
      startTime: completed?.startedAt ?? null,
      endTime: new Date().toISOString(),
      duration: completed?.durationMinutes ?? 0,
      score,
      notes: notes.trim(),
      energyRating,
      taskId: completed?.taskId ?? null,
    };

    const result = await saveSession(sessionData);

    if (result.ok) {
      setShowSessionForm(false);
      setScore(3);
      setNotes("");
      setEnergyRating(null);
      completedSessionRef.current = null;

      if (statusTimeoutRef.current) clearTimeout(statusTimeoutRef.current);
      statusTimeoutRef.current = setTimeout(() => setSubmitStatus("idle"), 3000);
    }
  }, [score, notes, energyRating, saveSession, setSubmitStatus]);

  const value = useMemo<PomodoroSessionContextValue>(
    () => ({
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
    }),
    [showSessionForm, score, notes, energyRating, isSubmitting, submitStatus, errorMessage, handleSaveSession],
  );

  return <PomodoroSessionContext.Provider value={value}>{children}</PomodoroSessionContext.Provider>;
}

export function usePomodoroSessionContext() {
  const ctx = useContext(PomodoroSessionContext);
  if (!ctx) {
    throw new Error("usePomodoroSessionContext must be used within a PomodoroSessionProvider");
  }
  return ctx;
}
