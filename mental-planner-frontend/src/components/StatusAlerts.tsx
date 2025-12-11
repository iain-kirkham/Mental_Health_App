import React from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { CheckCircle2, AlertCircle } from "lucide-react";

type Props = {
  submitStatus: 'idle' | 'success' | 'error';
  errorMessage?: string;
  showAlert?: boolean;
  alertMessage?: string;
};

export default function StatusAlerts({ submitStatus, errorMessage, showAlert, alertMessage }: Props) {
  return (
    <>
      {submitStatus === 'success' && (
        <Alert className="mb-6 bg-green-50 border-green-200 animate-in slide-in-from-top duration-300">
          <CheckCircle2 className="h-4 w-4 text-green-600" />
          <AlertDescription className="text-green-800 font-medium">🎉 Session saved successfully!</AlertDescription>
        </Alert>
      )}

      {submitStatus === 'error' && (
        <Alert variant="destructive" className="mb-6 animate-in slide-in-from-top duration-300">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription className="font-medium">{errorMessage || 'Failed to save session. Please try again.'}</AlertDescription>
        </Alert>
      )}

      {showAlert && (
        <Alert className="mb-6 bg-linear-to-r from-red-50 to-orange-50 border-red-300 animate-in slide-in-from-top duration-500">
          <AlertDescription className="text-red-700 font-semibold text-center">{alertMessage ?? '⏰ Time\'s up! Great work! 🎯'}</AlertDescription>
        </Alert>
      )}
    </>
  );
}

