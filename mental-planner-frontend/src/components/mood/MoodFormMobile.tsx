import React from "react";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { CheckCircle2, AlertCircle } from "lucide-react";
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
  setFormErrors: (fn: (prev: any) => any) => void;
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

export default function MoodFormMobile({ submitStatus, errorMessage, isSubmitting, selectedMood, setSelectedMood, formErrors, setFormErrors, date, setDate, time, setTime, factors, setFactors, newFactor, setNewFactor, showFactorInput, setShowFactorInput, notes, setNotes, handleSubmit, formatDate, }: Props) {
  return (
    <div className="md:hidden">
      <div className="w-full">
        <div className="space-y-6 p-6 bg-transparent max-w-md mx-auto pt-2 md:pt-0">
          {submitStatus === 'success' && (
            <Alert className="bg-green-50 border-green-200 animate-in slide-in-from-top duration-300">
              <CheckCircle2 className="h-4 w-4 text-green-600" />
              <AlertDescription className="text-green-800 font-medium">🎉 Mood entry saved successfully!</AlertDescription>
            </Alert>
          )}

          {submitStatus === 'error' && (
            <Alert variant="destructive" className="animate-in slide-in-from-top duration-300">
              <div className="flex items-start justify-between w-full gap-4">
                <div className="flex items-center gap-3">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription className="font-medium">{errorMessage || "We couldn't save your mood entry — please try again."}</AlertDescription>
                </div>
                <div className="shrink-0">
                  <Button size="sm" variant="outline" onClick={handleSubmit} disabled={isSubmitting} aria-label="Retry save">
                    Try again
                  </Button>
                </div>
              </div>
            </Alert>
          )}

          <MoodSelector selectedMood={selectedMood} setSelectedMood={setSelectedMood} isSubmitting={isSubmitting} />
          {formErrors.mood && (
            <p className={`text-sm ${submitStatus === 'error' ? 'text-red-600' : 'text-slate-600 dark:text-slate-400'}`}>{formErrors.mood}</p>
          )}

          <DateTimePickers date={date} setDate={setDate} time={time} setTime={setTime} isSubmitting={isSubmitting} formatDate={formatDate} />
          {(formErrors.date || formErrors.time) && (
            <div className="mt-2 space-y-1">
              {formErrors.date && <p className="text-sm text-red-600">{formErrors.date}</p>}
              {formErrors.time && <p className="text-sm text-red-600">{formErrors.time}</p>}
            </div>
          )}

          <FactorsSection factors={factors} setFactors={setFactors} newFactor={newFactor} setNewFactor={setNewFactor} showFactorInput={showFactorInput} setShowFactorInput={setShowFactorInput} isSubmitting={isSubmitting} formErrors={{ newFactor: formErrors.newFactor }} setFormErrors={setFormErrors} />

          <div>
            <label className="text-sm font-semibold text-slate-700 dark:text-slate-200 mb-2 block">📝 Notes</label>
            <Textarea placeholder="How are you feeling? What happened today?" value={notes} onChange={(e) => setNotes(e.target.value)} className="mt-1 min-h-[100px] border-2 focus:border-slate-400 transition-colors resize-none" disabled={isSubmitting} aria-label="Mood notes" />
          </div>
        </div>

        <div className="p-6 pt-0 max-w-md mx-auto">
          <Button className="w-full h-12 text-lg font-semibold bg-slate-800 text-white hover:opacity-95 transition-all duration-300 disabled:opacity-50" onClick={handleSubmit} disabled={selectedMood === null || isSubmitting} aria-label="Save mood entry">
            {isSubmitting ? (<><span className="animate-spin mr-2">⏳</span>Saving...</>) : (<><span className="mr-2">💾</span>Save Mood Entry</>)}
          </Button>
        </div>
      </div>
    </div>
  );
}

