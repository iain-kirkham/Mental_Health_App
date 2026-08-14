import React from "react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { CheckCircle2, AlertCircle } from "lucide-react";
import PageInset from "@/components/PageInset";
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

export default function MoodFormDesktop({ submitStatus, errorMessage, isSubmitting, selectedMood, setSelectedMood, formErrors, date, setDate, time, setTime, factors, setFactors, newFactor, setNewFactor, showFactorInput, setShowFactorInput, notes, setNotes, handleSubmit, formatDate, }: Props) {
  return (
    <div className="hidden md:block w-full">
      <PageInset size="wide">
        {submitStatus === 'success' && (
          <Alert className="mb-6 bg-green-50 dark:bg-green-950 border-green-200 dark:border-green-900 animate-in slide-in-from-top duration-300">
            <CheckCircle2 className="h-4 w-4 text-green-600 dark:text-green-400" />
            <AlertDescription className="text-green-800 dark:text-green-300 font-medium">🎉 Mood entry saved successfully!</AlertDescription>
          </Alert>
        )}

        {submitStatus === 'error' && (
          <Alert variant="destructive" className="mb-6 animate-in slide-in-from-top duration-300">
            <div className="flex items-start justify-between w-full gap-4">
              <div className="flex items-center gap-3">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription className="font-medium">{errorMessage || "We couldn't save your mood entry — please try again."}</AlertDescription>
              </div>
              <div className="flex-shrink-0">
                <Button size="sm" variant="outline" onClick={handleSubmit} disabled={isSubmitting} aria-label="Retry save">Try again</Button>
              </div>
            </div>
          </Alert>
        )}

        <div className="grid grid-cols-12 gap-6">
          <div className="col-span-7 space-y-6">
            <div className="p-6 rounded-md border border-border bg-card">
              <h3 className="text-sm font-semibold text-foreground mb-4">Select your mood</h3>
              <MoodSelector selectedMood={selectedMood} setSelectedMood={setSelectedMood} isSubmitting={isSubmitting} />
              {formErrors.mood && (<p className={`text-sm mt-2 ${submitStatus === 'error' ? 'text-destructive' : 'text-muted-foreground'}`}>{formErrors.mood}</p>)}
            </div>

            <div className="p-6 rounded-md border border-border bg-card">
              <h3 className="text-sm font-semibold text-foreground mb-4">📅 When</h3>
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

          <div className="col-span-5 p-6 rounded-md border border-border bg-card flex flex-col">
            <label className="text-sm font-semibold text-foreground mb-3 block">📝 Notes</label>
            <Textarea placeholder="How are you feeling? What happened today?" value={notes} onChange={(e) => setNotes(e.target.value)} className="mt-1 min-h-[360px] border-2 focus:border-ring transition-colors resize-none flex-1" disabled={isSubmitting} aria-label="Mood notes" />
            <Button className="mt-4 h-12 text-lg font-semibold hover:opacity-95 transition-all duration-200 disabled:opacity-50" onClick={handleSubmit} disabled={selectedMood === null || isSubmitting} aria-label="Save mood entry">{isSubmitting ? (<><span className="animate-spin mr-2">⏳</span>Saving...</>) : (<><span className="mr-2">💾</span>Save Mood Entry</>)}</Button>
          </div>
        </div>
      </PageInset>
    </div>
  );
}

