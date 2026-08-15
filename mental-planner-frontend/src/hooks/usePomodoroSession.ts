import { useState } from "react";
import { savePomodoroSession as apiSavePomodoroSession } from "@/lib/pomodoro-api";
import { toFriendlyMessage } from "@/lib/connectivity";
import type { PomodoroSessionCreationDTO } from "@/types";

export default function usePomodoroSession(getToken: () => Promise<string | null>) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitStatus, setSubmitStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState<string>('');

  const saveSession = async (session: PomodoroSessionCreationDTO) => {
    setIsSubmitting(true);
    setSubmitStatus('idle');
    setErrorMessage('');

    try {
      await apiSavePomodoroSession(session, getToken);
      setSubmitStatus('success');
      setIsSubmitting(false);
      return { ok: true } as const;
    } catch (error) {
      setSubmitStatus('error');
      setErrorMessage(toFriendlyMessage(error, 'An unexpected error occurred. Please try again.'));
      setIsSubmitting(false);
      return { ok: false, error } as const;
    }
  };

  return {
    isSubmitting,
    submitStatus,
    errorMessage,
    saveSession,
    setSubmitStatus,
    setErrorMessage,
  };
}

