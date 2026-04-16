import React from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

interface SessionSummaryModalProps {
    score: number;
    notes: string;
    onScoreChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onNotesChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
    onCancel: () => void;
    onSave: () => void;
    isSubmitting?: boolean;
}

export function SessionSummaryModal({
                                        score,
                                        notes,
                                        onScoreChange,
                                        onNotesChange,
                                        onCancel,
                                        onSave,
                                        isSubmitting = false,
                                    }: SessionSummaryModalProps) {
    const getScoreEmoji = (score: number) => {
        const emojis = ['😢', '😕', '😐', '😊', '🎉'];
        return emojis[score - 1] || '😐';
    };

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-in fade-in duration-300">
            <div className="bg-white dark:bg-gray-800 rounded-2xl p-8 max-w-md w-full shadow-2xl border border-gray-200 dark:border-gray-700 animate-in zoom-in duration-300">
                <div className="text-center mb-6">
                    <div className="text-4xl mb-2">✨</div>
                    <h2 className="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
                        Session Summary
                    </h2>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                        How did your focus session go?
                    </p>
                </div>

                <div className="space-y-5">
                    <div>
                        <label className="block mb-3 font-semibold text-slate-700 dark:text-slate-200">
                            {getScoreEmoji(score)} Your Score (1-5)
                        </label>
                        <Input
                            type="number"
                            min={1}
                            max={5}
                            value={score}
                            onChange={onScoreChange}
                            className="text-center text-2xl font-bold h-14 shadow-sm border-2"
                            disabled={isSubmitting}
                            aria-label="Session score"
                        />
                        <div className="flex justify-between mt-2 text-xs text-slate-500">
                            <span>😢 Poor</span>
                            <span>🎉 Excellent</span>
                        </div>
                    </div>

                    <div>
                        <label className="block mb-3 font-semibold text-slate-700 dark:text-slate-200">
                            📝 Notes (optional)
                        </label>
                        <textarea
                            value={notes}
                            onChange={onNotesChange}
                            rows={4}
                            placeholder="What helped you focus? Any distractions?"
                            className="w-full rounded-lg border-2 border-gray-300 dark:border-gray-600 p-3 dark:bg-gray-700 dark:text-white disabled:opacity-50 disabled:cursor-not-allowed focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all resize-none shadow-sm"
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
                        className="flex-1 h-12 font-semibold border-2 hover:bg-slate-50 dark:hover:bg-slate-800"
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={onSave}
                        disabled={isSubmitting}
                        className="flex-1 h-12 font-semibold bg-slate-600 hover:bg-slate-700 text-white focus-visible:ring-2 focus-visible:ring-slate-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
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
