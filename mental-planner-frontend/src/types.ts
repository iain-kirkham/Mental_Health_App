// Types for ADHD Focus Companion

// Pomodoro Session Types
export type EnergyRating = 'ENERGIZING' | 'DRAINING';

export interface PomodoroSessionCreationDTO {
    startTime: string | null;
    endTime: string;
    duration: number;
    score: number;
    notes: string;
    energyRating: EnergyRating | null;
}

export interface PomodoroSessionResponseDTO {
    id: number;
    startTime: string | null;
    endTime: string;
    duration: number;
    score: number;
    notes: string;
    energyRating: EnergyRating | null;
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

// Job Search Types
export type JobApplicationStatus = 'APPLIED' | 'INTERVIEWING' | 'OFFER' | 'REJECTED';

export interface JobApplicationRequestDTO {
    companyName: string;
    roleTitle: string;
    status: JobApplicationStatus;
}

export interface JobApplicationResponseDTO {
    id: number;
    companyName: string;
    roleTitle: string;
    status: JobApplicationStatus;
}

// UI Types
export interface MoodOption {
    value: number;
    icon: React.ReactNode;
    label: string;
    color: string;
}

