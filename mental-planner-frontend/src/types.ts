// Types for ADHD Focus Companion

// Pomodoro Session Types
export interface PomodoroSessionCreationDTO {
    startTime: string | null;
    endTime: string;
    duration: number;
    score: number;
    notes: string;
}

export interface PomodoroSessionResponseDTO {
    id: number;
    startTime: string | null;
    endTime: string;
    duration: number;
    score: number;
    notes: string;
}

// Mood Entry Types
export interface MoodEntryCreationDTO {
    moodScore: number;
    dateTime: string;
    factors: string[];
    notes: string;
}

export interface MoodEntryResponseDTO {
    id: number;
    moodScore: number;
    dateTime: string;
    factors: string[];
    notes: string;
}

// UI Types
export interface MoodOption {
    value: number;
    icon: React.ReactNode;
    label: string;
    color: string;
}

