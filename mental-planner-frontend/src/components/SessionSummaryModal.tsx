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
}

export function SessionSummaryModal({
                                        score,
                                        notes,
                                        onScoreChange,
                                        onNotesChange,
                                        onCancel,
                                        onSave,
                                    }: SessionSummaryModalProps) {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full shadow-lg">
                <h2 className="text-xl font-semibold mb-4">Session Summary</h2>

                <label className="block mb-2 font-medium">Score (1-5)</label>
                <Input
                    type="number"
                    min={1}
                    max={5}
                    value={score}
                    onChange={onScoreChange}
                    className="mb-4"
                />

                <label className="block mb-2 font-medium">Notes (optional)</label>
                <textarea
                    value={notes}
                    onChange={onNotesChange}
                    rows={4}
                    className="w-full rounded-md border border-gray-300 p-2 dark:bg-gray-700 dark:text-white"
                />

                <div className="flex justify-end space-x-4 mt-4">
                    <Button variant="outline" onClick={onCancel}>
                        Cancel
                    </Button>
                    <Button onClick={onSave}>Save Session</Button>
                </div>
            </div>
        </div>
    );
}
