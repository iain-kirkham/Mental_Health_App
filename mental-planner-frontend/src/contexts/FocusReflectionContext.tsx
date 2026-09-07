"use client";

import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { useAuth } from "@clerk/nextjs";
import useFocusReflection, { type CompletedFocusRun } from "@/hooks/useFocusReflection";
import { claimFocusEntry } from "@/lib/focusEntryHandoff";
import { useTimerStore } from "@/store/timerStore";
import type { EnergyRating } from "@/types";

type FocusReflectionContextValue = {
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

const FocusReflectionContext = createContext<FocusReflectionContextValue | null>(null);

// Focus-session time-tracking itself lives in useTimerStore (shared with the plain task
// stopwatch, so a task's tracked time has exactly one writer). This context owns only what's
// left once a focus session completes: the score/energy/notes reflection form, and attaching it
// to the entry TimerStoreBridge already logged for that run.
export function FocusReflectionProvider({ children }: { children: React.ReactNode }) {
  const { getToken } = useAuth();
  const { isSubmitting, submitStatus, errorMessage, saveReflection, setSubmitStatus } = useFocusReflection(getToken);

  const [showSessionForm, setShowSessionForm] = useState(false);
  const [score, setScore] = useState(3);
  const [notes, setNotes] = useState("");
  const [energyRating, setEnergyRating] = useState<EnergyRating | null>(null);

  // Captured from timerStore at the moment a focus session completes, since timerStore clears
  // its own session fields as part of that same completion - by the time the user fills in and
  // saves the reflection form, this ref is the only place that data is still available.
  const completedSessionRef = useRef<{ taskId: number | null; runKey: string | null; durationMinutes: number; startedAt: string | null } | null>(null);
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
    const entryId = completed?.runKey ? await claimFocusEntry(completed.runKey) : null;
    const completedRun: CompletedFocusRun = {
      taskId: completed?.taskId ?? null,
      durationMinutes: completed?.durationMinutes ?? 0,
      startedAt: completed?.startedAt ?? null,
    };

    const result = await saveReflection(entryId, completedRun, { score, notes: notes.trim(), energyRating });

    if (result.ok) {
      setShowSessionForm(false);
      setScore(3);
      setNotes("");
      setEnergyRating(null);
      completedSessionRef.current = null;

      if (statusTimeoutRef.current) clearTimeout(statusTimeoutRef.current);
      statusTimeoutRef.current = setTimeout(() => setSubmitStatus("idle"), 3000);
    }
  }, [score, notes, energyRating, saveReflection, setSubmitStatus]);

  const value = useMemo<FocusReflectionContextValue>(
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

  return <FocusReflectionContext.Provider value={value}>{children}</FocusReflectionContext.Provider>;
}

export function useFocusReflectionContext() {
  const ctx = useContext(FocusReflectionContext);
  if (!ctx) {
    throw new Error("useFocusReflectionContext must be used within a FocusReflectionProvider");
  }
  return ctx;
}
