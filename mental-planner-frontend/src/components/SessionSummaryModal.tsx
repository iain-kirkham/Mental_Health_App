import React from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import MoodOption from "@/components/mood/MoodOption";
import { getScoreEmoji, ENERGY_RATING_OPTIONS } from "@/lib/pomodoro-format";
import type { EnergyRating } from "@/types";

interface SessionSummaryModalProps {
    score: number;
    notes: string;
    energyRating: EnergyRating | null;
    onScoreChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onNotesChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
    onEnergyRatingChange: (rating: EnergyRating | null) => void;
    onCancel: () => void;
    onSave: () => void;
    isSubmitting?: boolean;
}

export function SessionSummaryModal({
                                        score,
                                        notes,
                                        energyRating,
                                        onScoreChange,
                                        onNotesChange,
                                        onEnergyRatingChange,
                                        onCancel,
                                        onSave,
                                        isSubmitting = false,
                                    }: SessionSummaryModalProps) {
    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 z-50 animate-in fade-in duration-300">
            <div className="bg-card rounded-lg p-8 max-w-md w-full shadow-lg border border-border animate-in zoom-in duration-200">
                <div className="mb-6">
                    <h2 className="text-xl font-semibold text-foreground">
                        How did that session go?
                    </h2>
                </div>

                <div className="space-y-5">
                    <div>
                        <label className="block mb-3 text-sm font-semibold text-foreground">
                            Score (1-5)
                        </label>
                        <div className="flex items-center gap-3">
                            <Input
                                type="number"
                                min={1}
                                max={5}
                                value={score}
                                onChange={onScoreChange}
                                className="w-20 text-center text-lg font-semibold h-11"
                                disabled={isSubmitting}
                                aria-label="Session score"
                            />
                            <span className="text-2xl" aria-hidden>{getScoreEmoji(score)}</span>
                        </div>
                    </div>

                    <div>
                        <label className="block mb-3 text-sm font-semibold text-foreground">
                            How did it leave you feeling? (optional)
                        </label>
                        <div className="flex gap-3">
                            {ENERGY_RATING_OPTIONS.map((option) => (
                                <MoodOption<EnergyRating>
                                    key={option.value}
                                    value={option.value}
                                    label={option.label}
                                    icon={option.icon}
                                    colorClass={option.colorClass}
                                    selected={energyRating === option.value}
                                    onSelect={(v) => onEnergyRatingChange(energyRating === v ? null : v)}
                                    disabled={isSubmitting}
                                    ariaLabel={option.ariaLabel}
                                />
                            ))}
                        </div>
                    </div>

                    <div>
                        <label className="block mb-3 text-sm font-semibold text-foreground">
                            Notes (optional)
                        </label>
                        <textarea
                            value={notes}
                            onChange={onNotesChange}
                            rows={4}
                            placeholder="What helped you focus? Any distractions?"
                            className="w-full rounded-md border border-input bg-transparent p-3 text-sm text-foreground disabled:opacity-50 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring transition-colors resize-none"
                            disabled={isSubmitting}
                            aria-label="Session notes"
                        />
                    </div>
                </div>

                <div className="flex gap-3 mt-8">
                    <Button
                        variant="outline"
                        onClick={onCancel}
                        disabled={isSubmitting}
                        className="flex-1 h-11 font-medium"
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={onSave}
                        disabled={isSubmitting}
                        className="flex-1 h-11 font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
                    >
                        {isSubmitting ? 'Saving…' : 'Save session'}
                    </Button>
                </div>
            </div>
        </div>
    );
}
