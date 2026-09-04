import React, { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { X, Plus } from "lucide-react";

const COMMON_FACTORS = ["Work", "Sleep", "Exercise", "Social", "Nutrition", "Weather"];

type Props = {
  factors: string[];
  setFactors: (f: string[]) => void;
  newFactor: string;
  setNewFactor: (s: string) => void;
  showFactorInput: boolean;
  setShowFactorInput: (b: boolean) => void;
  isSubmitting: boolean;
  formErrors: { newFactor?: string };
};

export default function FactorsSection({ factors, setFactors, newFactor, setNewFactor, showFactorInput, setShowFactorInput, isSubmitting, formErrors }: Props) {
  const [addError, setAddError] = useState<string>();

  const handleAdd = () => {
    const trimmed = newFactor.trim();
    if (!trimmed) {
      setAddError('Type a factor (e.g. Sleep) and press Add.');
      return;
    }
    if (factors.includes(trimmed)) {
      setAddError('That factor is already added.');
      return;
    }
    setFactors([...factors, trimmed]);
    setNewFactor("");
    setShowFactorInput(false);
    setAddError(undefined);
  };

  return (
    <div className="bg-muted/40 p-4 rounded-lg border border-border">
      <div className="flex justify-between items-center mb-3">
        <h3 className="text-sm font-semibold text-foreground">Contributing factors</h3>
        <Button variant="ghost" size="sm" onClick={() => setShowFactorInput(!showFactorInput)} disabled={isSubmitting} aria-label="Add custom factor" className="h-8 w-8 p-0 hover:bg-accent">
          <Plus size={18} className={showFactorInput ? "rotate-45 transition-transform" : "transition-transform"} />
        </Button>
      </div>

      <div className="flex flex-wrap gap-2 mb-3">
        {COMMON_FACTORS.map(factor => (
          <Badge key={factor} variant="outline" className={`cursor-pointer hover:bg-accent hover:border-ring transition-colors duration-200 ${isSubmitting ? 'opacity-50 cursor-not-allowed' : ''} ${factors.includes(factor) ? 'opacity-50' : ''}`} onClick={() => !isSubmitting && !factors.includes(factor) && setFactors([...factors, factor])}>
            {factor}
          </Badge>
        ))}
      </div>

      {factors.length > 0 && (
        <div className="flex flex-wrap gap-2 mb-2 p-3 bg-card rounded-md border border-border">
          {factors.map(factor => (
            <Badge key={factor} className="bg-primary text-primary-foreground flex items-center gap-1 px-3 py-1 hover:opacity-90 transition-opacity">
              {factor}
              <X size={14} className={`cursor-pointer transition-opacity hover:opacity-70 ${isSubmitting ? 'pointer-events-none' : ''}`} onClick={() => !isSubmitting && setFactors(factors.filter(f => f !== factor))} aria-label={`Remove ${factor}`} />
            </Badge>
          ))}
        </div>
      )}

      {showFactorInput && (
        <div className="flex gap-2 mt-3 animate-in slide-in-from-top duration-300">
          <Input placeholder="Add custom factor..." value={newFactor} onChange={(e) => { setNewFactor(e.target.value); setAddError(undefined); }} onKeyDown={(e) => e.key === 'Enter' && handleAdd()} disabled={isSubmitting} aria-label="Custom factor name" />
          <Button size="sm" onClick={handleAdd} disabled={isSubmitting || !newFactor.trim()} aria-label="Add factor">
            <Plus size={16} />
          </Button>
        </div>
      )}
      {(addError || formErrors.newFactor) && <p className="text-sm text-destructive mt-2">{addError || formErrors.newFactor}</p>}
    </div>
  );
}

