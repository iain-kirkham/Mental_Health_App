import React from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
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
    const getScoreEmoji = (score: number) => {
        const emojis = ['😢', '😕', '😐', '😊', '🎉'];
        return emojis[score - 1] || '😐';
    };

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 z-50 animate-in fade-in duration-300">
            <div className="bg-card rounded-2xl p-8 max-w-md w-full shadow-2xl border border-border animate-in zoom-in duration-300">
                <div className="text-center mb-6">
                    <div className="text-4xl mb-2">✨</div>
                    <h2 className="text-2xl font-bold bg-linear-to-r from-primary to-[hsl(var(--chart-1))] bg-clip-text text-transparent">
                        Session Summary
                    </h2>
                    <p className="text-sm text-muted-foreground mt-1">
                        How did your focus session go?
                    </p>
                </div>

                <div className="space-y-5">
                    <div>
                        <label className="block mb-3 font-semibold text-foreground">
                            {getScoreEmoji(score)} Your Score (1-5)
                        </label>
                        <Input
                            type="number"
                            min={1}
                            max={5}
                            value={score}
                            onChange={onScoreChange}
                            className="text-center text-2xl font-bold h-14 shadow-xs border-2"
                            disabled={isSubmitting}
                            aria-label="Session score"
                        />
                        <div className="flex justify-between mt-2 text-xs text-muted-foreground">
                            <span>😢 Poor</span>
                            <span>🎉 Excellent</span>
                        </div>
                    </div>

                    <div>
                        <label className="block mb-3 font-semibold text-foreground">
                            🔋 How did it leave you feeling? (optional)
                        </label>
                        <div className="flex gap-3">
                            <button
                                type="button"
                                onClick={() => onEnergyRatingChange(energyRating === 'ENERGIZING' ? null : 'ENERGIZING')}
                                disabled={isSubmitting}
                                aria-pressed={energyRating === 'ENERGIZING'}
                                className={`flex-1 h-12 rounded-lg border-2 font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed ${
                                    energyRating === 'ENERGIZING'
                                        ? 'border-emerald-500 bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300'
                                        : 'border-border text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                                }`}
                            >
                                ⚡ Energizing
                            </button>
                            <button
                                type="button"
                                onClick={() => onEnergyRatingChange(energyRating === 'DRAINING' ? null : 'DRAINING')}
                                disabled={isSubmitting}
                                aria-pressed={energyRating === 'DRAINING'}
                                className={`flex-1 h-12 rounded-lg border-2 font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed ${
                                    energyRating === 'DRAINING'
                                        ? 'border-amber-500 bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-300'
                                        : 'border-border text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                                }`}
                            >
                                🪫 Draining
                            </button>
                        </div>
                    </div>

                    <div>
                        <label className="block mb-3 font-semibold text-foreground">
                            📝 Notes (optional)
                        </label>
                        <textarea
                            value={notes}
                            onChange={onNotesChange}
                            rows={4}
                            placeholder="What helped you focus? Any distractions?"
                            className="w-full rounded-lg border-2 border-input bg-transparent p-3 text-foreground disabled:opacity-50 disabled:cursor-not-allowed focus:border-ring focus:ring-2 focus:ring-ring/30 transition-all resize-none shadow-xs"
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
                        className="flex-1 h-12 font-semibold border-2"
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={onSave}
                        disabled={isSubmitting}
                        className="flex-1 h-12 font-semibold disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
                    >
                        {isSubmitting ? (
                            <>
                                <span className="animate-spin mr-2">⏳</span>
                                Saving...
                            </>
                        ) : (
                            <>
                                <span className="mr-2">💾</span>
                                Save Session
                            </>
                        )}
                    </Button>
                </div>
            </div>
        </div>
    );
}
