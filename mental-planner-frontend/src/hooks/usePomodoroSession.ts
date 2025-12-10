import { useState } from "react";
import { savePomodoroSession as apiSavePomodoroSession } from "@/lib/pomodoro-api";
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
      const response = await apiSavePomodoroSession(session, getToken);

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const errorMsg = errorData.message || `Failed to save session (Status: ${response.status})`;
        setSubmitStatus('error');
        setErrorMessage(errorMsg);
        setIsSubmitting(false);
        return { ok: false, response } as const;
      }

      await response.json();
      setSubmitStatus('success');
      setIsSubmitting(false);
      return { ok: true, response } as const;
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : 'An unexpected error occurred';
      setSubmitStatus('error');
      setErrorMessage(errorMsg);
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

