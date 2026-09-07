// Types for ADHD Focus Companion

// Focus session rating types
export type EnergyRating = 'ENERGIZING' | 'DRAINING';

// Task Types (daily planner)
export type TaskPriority = 'URGENT' | 'HIGH' | 'NORMAL' | 'LOW';

export interface TaskRequestDTO {
    title: string;
    description: string | null;
    scheduledDate: string; // "YYYY-MM-DD"
    startTime: string | null; // ISO instant
    endTime: string | null; // ISO instant
    completed: boolean;
    sortOrder: number;
    plannedMinutes: number | null;
    actualMinutes: number;
    category: string | null;
    archived: boolean;
    priority: TaskPriority;
}

export interface TaskResponseDTO extends TaskRequestDTO {
    id: number;
    subtasks: SubtaskResponseDTO[];
}

export interface TaskReorderItemDTO {
    id: number;
    sortOrder: number;
}

export interface SubtaskRequestDTO {
    title: string;
    completed: boolean;
    sortOrder: number;
    plannedMinutes: number | null;
}

export interface SubtaskResponseDTO extends SubtaskRequestDTO {
    id: number;
    taskId: number;
}

export type TimeEntrySource = 'STOPWATCH' | 'MANUAL' | 'FOCUS';

export interface TaskTimeEntryRequestDTO {
    taskId: number | null; // null for a task-less Focus session
    startedAt: string | null; // ISO instant, null for manual entries
    endedAt: string | null; // ISO instant, null for manual entries
    minutes: number;
    entryDate: string; // "YYYY-MM-DD"
    source: TimeEntrySource;
    notes: string | null;
    score: number | null; // Focus session quality, 1-5
    energyRating: EnergyRating | null;
}

export interface TaskTimeEntryResponseDTO extends TaskTimeEntryRequestDTO {
    id: number;
}

export interface TimeEntryReflectionRequestDTO {
    score: number | null;
    notes: string | null;
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

// UI Types
export interface MoodOption {
    value: number;
    icon: React.ReactNode;
    label: string;
    color: string;
}

