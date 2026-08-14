"use client";

import { SessionSummaryModal } from "@/components/SessionSummaryModal";
import { usePomodoroSessionContext } from "@/contexts/PomodoroSessionContext";

// Rendered once at the app root so the session summary prompt still appears
// even if the user navigated away from the Pomodoro page before it expired.
export function GlobalPomodoroModal() {
  const {
    showSessionForm,
    setShowSessionForm,
    score,
    setScore,
    notes,
    setNotes,
    energyRating,
    setEnergyRating,
    isSubmitting,
    handleSaveSession,
  } = usePomodoroSessionContext();

  if (!showSessionForm) return null;

  return (
    <SessionSummaryModal
      score={score}
      notes={notes}
      energyRating={energyRating}
      onScoreChange={(e) => setScore(Number(e.target.value))}
      onNotesChange={(e) => setNotes(e.target.value)}
      onEnergyRatingChange={setEnergyRating}
      onCancel={() => setShowSessionForm(false)}
      onSave={handleSaveSession}
      isSubmitting={isSubmitting}
    />
  );
}
