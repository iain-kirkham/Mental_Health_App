import { useEffect, useRef, useState } from "react";
import usePomodoroSession from "@/hooks/usePomodoroSession";
import type { EnergyRating, PomodoroSessionCreationDTO } from "@/types";

export default function useSessionManager(getToken: () => Promise<string | null>) {
  const [showSessionForm, setShowSessionForm] = useState(false);
  const [score, setScore] = useState<number>(3);
  const [notes, setNotes] = useState<string>("");
  const [energyRating, setEnergyRating] = useState<EnergyRating | null>(null);
  const statusTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const { isSubmitting, submitStatus, errorMessage, saveSession, setSubmitStatus } = usePomodoroSession(getToken);

  // Clean up timeout on unmount
  useEffect(() => {
    return () => {
      if (statusTimeoutRef.current) clearTimeout(statusTimeoutRef.current);
    };
  }, []);

  const handleSaveSession = async (params: { startTime: Date | null; totalTime: number; timeLeft: number }) => {
    const { startTime, totalTime, timeLeft } = params;

    const sessionData: PomodoroSessionCreationDTO = {
      startTime: startTime?.toISOString() ?? null,
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

      // Auto-clear status
      if (statusTimeoutRef.current) clearTimeout(statusTimeoutRef.current);
      statusTimeoutRef.current = setTimeout(() => setSubmitStatus('idle'), 3000);
    }

    return result;
  };

  return {
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
}

