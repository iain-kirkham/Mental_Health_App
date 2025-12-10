import { useState } from "react";
import usePomodoroSession from "@/hooks/usePomodoroSession";
import type { PomodoroSessionCreationDTO } from "@/types";

export default function useSessionManager(getToken: () => Promise<string | null>) {
  const [showSessionForm, setShowSessionForm] = useState(false);
  const [score, setScore] = useState<number>(3);
  const [notes, setNotes] = useState<string>("");

  const { isSubmitting, submitStatus, errorMessage, saveSession, setSubmitStatus } = usePomodoroSession(getToken);

  const handleSaveSession = async (params: { startTime: Date | null; totalTime: number; timeLeft: number }) => {
    const { startTime, totalTime, timeLeft } = params;

    const sessionData: PomodoroSessionCreationDTO = {
      startTime: startTime?.toISOString() ?? null,
      endTime: new Date().toISOString(),
      duration: Math.round((totalTime - timeLeft) / 60),
      score,
      notes: notes.trim(),
    };

    const result = await saveSession(sessionData);

    if (result.ok) {
      setShowSessionForm(false);
      setScore(3);
      setNotes("");

      // Auto-clear status
      setTimeout(() => setSubmitStatus('idle'), 3000);
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
    isSubmitting,
    submitStatus,
    errorMessage,
    handleSaveSession,
  };
}

