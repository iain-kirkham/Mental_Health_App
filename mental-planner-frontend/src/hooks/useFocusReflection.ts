import { useState } from "react";
import { logTimeEntry, updateTimeEntryReflection } from "@/lib/tasks-api";
import { toFriendlyMessage } from "@/lib/connectivity";
import type { EnergyRating, TaskTimeEntryRequestDTO } from "@/types";

export type FocusReflectionData = {
  score: number;
  notes: string;
  energyRating: EnergyRating | null;
};

export type CompletedFocusRun = {
  taskId: number | null;
  durationMinutes: number;
  startedAt: string | null;
};

export default function useFocusReflection(getToken: () => Promise<string | null>) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitStatus, setSubmitStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState<string>('');

  /**
   * Attaches a reflection to the entry the just-completed run already logged (via
   * claimFocusEntry). If that entry's id isn't available - the immediate create-POST failed,
   * or the run predates this handoff (e.g. survived a reload) - falls back to a fresh POST
   * carrying the reflection fields, so the user's input is never silently dropped.
   */
  const saveReflection = async (
    entryId: number | null,
    completed: CompletedFocusRun,
    reflection: FocusReflectionData
  ) => {
    setIsSubmitting(true);
    setSubmitStatus('idle');
    setErrorMessage('');

    try {
      if (entryId !== null) {
        await updateTimeEntryReflection(
          entryId,
          { score: reflection.score, notes: reflection.notes || null, energyRating: reflection.energyRating },
          getToken
        );
      } else {
        const fallbackEntry: TaskTimeEntryRequestDTO = {
          taskId: completed.taskId,
          startedAt: completed.startedAt,
          endedAt: new Date().toISOString(),
          minutes: completed.durationMinutes,
          entryDate: completed.startedAt ? completed.startedAt.slice(0, 10) : new Date().toISOString().slice(0, 10),
          source: 'FOCUS',
          notes: reflection.notes || null,
          score: reflection.score,
          energyRating: reflection.energyRating,
        };
        await logTimeEntry(fallbackEntry, getToken);
      }
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
    saveReflection,
    setSubmitStatus,
    setErrorMessage,
  };
}
