import React from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { CheckCircle2, AlertCircle } from "lucide-react";
import { cn } from "@/lib/utils";

type Props = {
  submitStatus: 'idle' | 'success' | 'error';
  errorMessage?: string;
  showAlert?: boolean;
  alertMessage?: string;
  /** Copy for the success banner. */
  successMessage?: string;
  /** Fallback copy when the error carries no message of its own. */
  errorFallback?: string;
  /** Optional control (e.g. a retry button) shown alongside the error text. */
  errorAction?: React.ReactNode;
  /** Spacing override — callers inside a `space-y-*` stack pass `""`. */
  className?: string;
};

export default function StatusAlerts({
  submitStatus,
  errorMessage,
  showAlert,
  alertMessage,
  successMessage = '🎉 Session saved successfully!',
  errorFallback = 'Failed to save session. Please try again.',
  errorAction,
  className = 'mb-3',
}: Props) {
  return (
    <>
      {submitStatus === 'success' && (
        <Alert className={cn(className, 'bg-chart-2/10 border-chart-2/40 animate-in slide-in-from-top duration-300')}>
          <CheckCircle2 className="h-4 w-4 text-chart-2" />
          <AlertDescription className="text-chart-2 font-medium">{successMessage}</AlertDescription>
        </Alert>
      )}

      {submitStatus === 'error' && (
        <Alert variant="destructive" className={cn(className, 'animate-in slide-in-from-top duration-300')}>
          <div className="flex items-start justify-between w-full gap-4">
            <div className="flex items-center gap-3">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription className="font-medium">{errorMessage || errorFallback}</AlertDescription>
            </div>
            {errorAction && <div className="flex-shrink-0">{errorAction}</div>}
          </div>
        </Alert>
      )}

      {showAlert && (
        <Alert className={cn(className, 'bg-chart-3/10 border-chart-3/40 animate-in slide-in-from-top duration-500')}>
          <AlertDescription className="text-chart-3 font-semibold text-center">{alertMessage ?? '⏰ Time\'s up! Great work! 🎯'}</AlertDescription>
        </Alert>
      )}
    </>
  );
}
