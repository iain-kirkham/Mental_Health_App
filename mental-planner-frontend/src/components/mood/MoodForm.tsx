import React from "react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import StatusAlerts from "@/components/StatusAlerts";
import MoodSelector from "@/components/mood/MoodSelector";
import DateTimePickers from "@/components/mood/DateTimePickers";
import FactorsSection from "@/components/mood/FactorsSection";
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

export default function MoodForm({ submitStatus, errorMessage, isSubmitting, selectedMood, setSelectedMood, formErrors, date, setDate, time, setTime, factors, setFactors, newFactor, setNewFactor, showFactorInput, setShowFactorInput, notes, setNotes, handleSubmit, formatDate, }: Props) {
  return (
    <div className="w-full px-3 py-3 md:px-4">
      <StatusAlerts
        submitStatus={submitStatus}
        errorMessage={errorMessage}
        className="mb-6 max-w-md lg:max-w-none mx-auto lg:mx-0"
        successMessage="Mood entry saved."
        errorFallback="We couldn't save your mood entry — please try again."
        errorAction={<Button size="sm" variant="outline" onClick={handleSubmit} disabled={isSubmitting} aria-label="Retry save">Try again</Button>}
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-stretch max-w-md lg:max-w-none mx-auto lg:mx-0">
        <div className="space-y-6">
          <div className="border-b border-border pb-6">
            <h3 className="text-sm font-semibold text-foreground mb-4">Select your mood</h3>
            <MoodSelector selectedMood={selectedMood} setSelectedMood={setSelectedMood} isSubmitting={isSubmitting} />
            {formErrors.mood && (<p className={`text-sm mt-2 ${submitStatus === 'error' ? 'text-destructive' : 'text-muted-foreground'}`}>{formErrors.mood}</p>)}
          </div>

          <div className="border-b border-border pb-6">
            <h3 className="text-sm font-semibold text-foreground mb-4">When</h3>
            <DateTimePickers date={date} setDate={setDate} time={time} setTime={setTime} isSubmitting={isSubmitting} formatDate={formatDate} />
            {(formErrors.date || formErrors.time) && (
              <div className="mt-2 space-y-1">
                {formErrors.date && <p className="text-sm text-destructive">{formErrors.date}</p>}
                {formErrors.time && <p className="text-sm text-destructive">{formErrors.time}</p>}
              </div>
            )}
          </div>

          <FactorsSection factors={factors} setFactors={setFactors} newFactor={newFactor} setNewFactor={setNewFactor} showFactorInput={showFactorInput} setShowFactorInput={setShowFactorInput} isSubmitting={isSubmitting} formErrors={{ newFactor: formErrors.newFactor }} />
        </div>

        <div className="lg:border-l border-border lg:pl-6 flex flex-col">
          <label className="text-sm font-semibold text-foreground mb-3 block">Notes</label>
          <Textarea placeholder="How are you feeling? What happened today?" value={notes} onChange={(e) => setNotes(e.target.value)} className="mt-1 min-h-[100px] lg:min-h-[240px] resize-none lg:flex-1" disabled={isSubmitting} aria-label="Mood notes" />
          <Button className="mt-4 h-11 font-medium disabled:opacity-50" onClick={handleSubmit} disabled={selectedMood === null || isSubmitting} aria-label="Save mood entry">{isSubmitting ? 'Saving…' : 'Save entry'}</Button>
        </div>
      </div>
    </div>
  );
}
