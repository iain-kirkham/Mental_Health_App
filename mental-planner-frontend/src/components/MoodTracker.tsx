'use client'

import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { CalendarIcon, Clock, Frown, Smile, Meh, Plus, X } from "lucide-react";

// Mood configuration (unchanged)
const MOOD_OPTIONS = [
    { value: 1, icon: <Frown size={32} strokeWidth={2.5} />, label: "Very Bad", color: "bg-red-100 hover:bg-red-200 border-red-300" },
    { value: 2, icon: <Frown size={32} />, label: "Bad", color: "bg-orange-100 hover:bg-orange-200 border-orange-300" },
    { value: 3, icon: <Meh size={32} />, label: "Okay", color: "bg-yellow-100 hover:bg-yellow-200 border-yellow-300" },
    { value: 4, icon: <Smile size={32} />, label: "Good", color: "bg-green-100 hover:bg-green-200 border-green-300" },
    { value: 5, icon: <Smile size={32} strokeWidth={2.5} />, label: "Very Good", color: "bg-emerald-100 hover:bg-emerald-200 border-emerald-300" },
];

const COMMON_FACTORS = ["Work", "Sleep", "Exercise", "Social", "Nutrition", "Weather"];

export default function MoodTracker() {
    const [date, setDate] = useState<Date | undefined>(new Date()); // Allow undefined
    const [time, setTime] = useState<string>(formatTime(new Date()));
    const [selectedMood, setSelectedMood] = useState<number | null>(null);
    const [notes, setNotes] = useState<string>("");
    const [factors, setFactors] = useState<string[]>([]);
    const [newFactor, setNewFactor] = useState<string>('');
    const [showFactorInput, setShowFactorInput] = useState<boolean>(false);

    function formatDate(date: Date | undefined): string {
        if (!date) {
            return "Pick a date";
        }
        const options: Intl.DateTimeFormatOptions = { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' };
        return date.toLocaleDateString('en-GB', options);
    }

    // *** UPDATED formatTime FUNCTION ***
    function formatTime(date: Date | undefined): string {
        if (!date) {
            return "--:--"; // Or some other placeholder
        }
        return date.toTimeString().slice(0, 5);
    }

    function getMoodColorClass() {
        const mood = MOOD_OPTIONS.find(m => m.value === selectedMood);
        return mood ? mood.color : "";
    }

    // Event handlers
    const handleAddFactor = () => {
        if (newFactor.trim()) {
            setFactors([...factors, newFactor.trim()]);
            setNewFactor("");
            setShowFactorInput(false);
        }
    };

    const handleSubmit = async () => {
        try {
            let dateTime: string;

            try {
                // Ensure date is a valid Date object AND not an "Invalid Date"
                if (date instanceof Date && !isNaN(date.getTime()) && typeof time === 'string' && time) {
                    const [hours, minutes] = time.split(':').map(Number);

                    // Add validation for parsed hours/minutes
                    if (isNaN(hours) || isNaN(minutes) || hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                        console.warn("Invalid time format. Using current date/time as fallback.");
                        dateTime = new Date().toISOString();
                    } else {
                        const dateObj = new Date(date);
                        dateObj.setHours(hours);
                        dateObj.setMinutes(minutes);
                        dateObj.setSeconds(0);
                        dateObj.setMilliseconds(0);
                        dateTime = dateObj.toISOString();
                    }
                } else {
                    console.warn("Invalid date or time provided. Using current date/time as fallback.");
                    dateTime = new Date().toISOString();
                }
            } catch (dateError) {
                console.error("Error constructing dateTime:", dateError);
                dateTime = new Date().toISOString();
            }

            console.log("Selected Date:", date, "Selected Time:", time, "Combined DateTime:", dateTime);

            const moodEntry = {
                moodScore: selectedMood,
                dateTime: dateTime,
                factors: factors,
                notes: notes
            };

            console.log("Saving mood entry:", moodEntry);

            const response = await fetch('http://localhost:8080/api/mood', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(moodEntry)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const savedEntry = await response.json();
            console.log("Mood entry saved successfully:", savedEntry);

            // Reset form after a successful save
            setSelectedMood(null);
            setNotes("");
            setFactors([]);
            setDate(new Date()); // Reset to the current date
            setTime(formatTime(new Date())); // Reset to the current time

        } catch (error) {
            console.error("Error saving mood entry:", error);
        }
    };

    const renderMoodSelector = () => (
        <div className="flex justify-between items-center py-2">
            {MOOD_OPTIONS.map((mood) => (
                <Button
                    key={mood.value}
                    variant="outline"
                    className={`flex flex-col items-center p-4 min-h-16 transition-all ${
                        selectedMood === mood.value ? `${mood.color} border-2` : ""
                    }`}
                    onClick={() => setSelectedMood(mood.value)}
                >
                    <div className={selectedMood === mood.value ? "text-black" : "text-gray-500"}>
                        {mood.icon}
                    </div>
                    <span className="text-xs mt-1">{mood.label}</span>
                </Button>
            ))}
        </div>
    );

    const renderDateTimePickers = () => (
        <div className="flex gap-2">
            <Popover>
                <PopoverTrigger asChild>
                    <Button variant="outline" className="flex justify-start items-center w-full">
                        <CalendarIcon className="mr-2 h-4 w-4" />
                        <span>{formatDate(date)}</span> {/* This now handles undefined */}
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0">
                    {/* onSelect takes a Date | undefined, which setDate can now handle */}
                    <Calendar mode="single" selected={date} onSelect={setDate} initialFocus />
                </PopoverContent>
            </Popover>

            <Popover>
                <PopoverTrigger asChild>
                    <Button variant="outline" className="flex justify-start items-center">
                        <Clock className="mr-2 h-4 w-4" />
                        <span>{time}</span>
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="p-4 w-auto">
                    <div className="space-y-2">
                        <h4 className="font-medium">Select time</h4>
                        <Input type="time" value={time} onChange={(e) => setTime(e.target.value)} />
                    </div>
                </PopoverContent>
            </Popover>
        </div>
    );

    const renderFactorsSection = () => (
        <div>
            <div className="flex justify-between items-center mb-2">
                <h3 className="text-sm font-medium">Factors</h3>
                <Button variant="ghost" size="sm" onClick={() => setShowFactorInput(!showFactorInput)}>
                    <Plus size={16} />
                </Button>
            </div>

            {/* Common factors */}
            <div className="flex flex-wrap gap-1 mb-2">
                {COMMON_FACTORS.map(factor => (
                    <Badge
                        key={factor}
                        variant="outline"
                        className="cursor-pointer hover:bg-accent"
                        onClick={() => !factors.includes(factor) && setFactors([...factors, factor])}
                    >
                        {factor}
                    </Badge>
                ))}
            </div>

            {/* Selected factors */}
            <div className="flex flex-wrap gap-1 mb-2">
                {factors.map(factor => (
                    <Badge key={factor} variant="secondary" className="flex items-center gap-1">
                        {factor}
                        <X
                            size={12}
                            className="cursor-pointer"
                            onClick={() => setFactors(factors.filter(f => f !== factor))}
                        />
                    </Badge>
                ))}
            </div>

            {/* Add a custom factor */}
            {showFactorInput && (
                <div className="flex gap-2 mt-2">
                    <Input
                        placeholder="Add custom factor..."
                        value={newFactor}
                        onChange={(e) => setNewFactor(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleAddFactor()}
                    />
                    <Button size="sm" onClick={handleAddFactor}>
                        <X size={16} className="rotate-45" />
                    </Button>
                </div>
            )}
        </div>
    );

    return (
        <Card className="w-full max-w-md mx-auto">
            <CardHeader className={selectedMood ? `${getMoodColorClass()} rounded-t-lg transition-colors duration-300` : ""}>
                <CardTitle>How are you feeling?</CardTitle>
                <CardDescription>Track your mood and contributing factors</CardDescription>
            </CardHeader>

            <CardContent className="space-y-4">
                {renderMoodSelector()}
                {renderDateTimePickers()}
                {renderFactorsSection()}

                <div>
                    <label className="text-sm font-medium">Notes</label>
                    <Textarea
                        placeholder="How are you feeling? What happened today?"
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        className="mt-1"
                    />
                </div>
            </CardContent>

            <CardFooter>
                <Button
                    className="w-full"
                    onClick={handleSubmit}
                    disabled={selectedMood === null}
                >
                    Save Mood Entry
                </Button>
            </CardFooter>
        </Card>
    );
}