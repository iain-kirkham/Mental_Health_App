import React from "react";
import { Button } from "@/components/ui/button";
import StatusAlerts from "@/components/StatusAlerts";
import MoodSelector from "@/components/mood/MoodSelector";
import DateTimePickers from "@/components/mood/DateTimePickers";
import FactorsSection from "@/components/mood/FactorsSection";
import { Textarea } from "@/components/ui/textarea";
import type { FormErrors } from "@/components/mood/types";

type Props = {
  submitStatus: 'idle' | 'success' | 'error';
  errorMessage: string;
  isSubmitting: boolean;
  selectedMood: number | null;
  setSelectedMood: (v: number | null) => void;
  formErrors: FormErrors;
  date: Date | undefined;
  setDate: (d: Date | undefined) => void;
  time: string;
  setTime: (t: string) => void;
  factors: string[];
  setFactors: (f: string[]) => void;
  newFactor: string;
  setNewFactor: (s: string) => void;
  showFactorInput: boolean;
  setShowFactorInput: (b: boolean) => void;
  notes: string;
  setNotes: (n: string) => void;
  handleSubmit: () => Promise<void>;
  formatDate: (d: Date | undefined) => string;
};

export default function MoodFormMobile({ submitStatus, errorMessage, isSubmitting, selectedMood, setSelectedMood, formErrors, date, setDate, time, setTime, factors, setFactors, newFactor, setNewFactor, showFactorInput, setShowFactorInput, notes, setNotes, handleSubmit, formatDate, }: Props) {
  return (
    <div className="md:hidden">
      <div className="w-full">
        <div className="space-y-4 px-3 py-3 max-w-md mx-auto">
          <StatusAlerts
            submitStatus={submitStatus}
            errorMessage={errorMessage}
            className=""
            successMessage="🎉 Mood entry saved successfully!"
            errorFallback="We couldn't save your mood entry — please try again."
            errorAction={
              <Button size="sm" variant="outline" onClick={handleSubmit} disabled={isSubmitting} aria-label="Retry save">
                Try again
              </Button>
            }
          />

          <MoodSelector selectedMood={selectedMood} setSelectedMood={setSelectedMood} isSubmitting={isSubmitting} />
          {formErrors.mood && (
            <p className={`text-sm ${submitStatus === 'error' ? 'text-destructive' : 'text-muted-foreground'}`}>{formErrors.mood}</p>
          )}

          <DateTimePickers date={date} setDate={setDate} time={time} setTime={setTime} isSubmitting={isSubmitting} formatDate={formatDate} />
          {(formErrors.date || formErrors.time) && (
            <div className="mt-2 space-y-1">
              {formErrors.date && <p className="text-sm text-destructive">{formErrors.date}</p>}
              {formErrors.time && <p className="text-sm text-destructive">{formErrors.time}</p>}
            </div>
          )}

          <FactorsSection factors={factors} setFactors={setFactors} newFactor={newFactor} setNewFactor={setNewFactor} showFactorInput={showFactorInput} setShowFactorInput={setShowFactorInput} isSubmitting={isSubmitting} formErrors={{ newFactor: formErrors.newFactor }} />

          <div>
            <label className="text-sm font-semibold text-foreground mb-2 block">📝 Notes</label>
            <Textarea placeholder="How are you feeling? What happened today?" value={notes} onChange={(e) => setNotes(e.target.value)} className="mt-1 min-h-[100px] border-2 focus:border-ring transition-colors resize-none" disabled={isSubmitting} aria-label="Mood notes" />
          </div>
        </div>

        <div className="px-3 pb-3 max-w-md mx-auto">
          <Button className="w-full h-12 text-lg font-semibold hover:opacity-95 transition-all duration-300 disabled:opacity-50" onClick={handleSubmit} disabled={selectedMood === null || isSubmitting} aria-label="Save mood entry">
            {isSubmitting ? (<><span className="animate-spin mr-2">⏳</span>Saving...</>) : (<><span className="mr-2">💾</span>Save Mood Entry</>)}
          </Button>
        </div>
      </div>
    </div>
  );
}

