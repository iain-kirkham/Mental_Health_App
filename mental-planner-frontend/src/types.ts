export interface SubTask {
    id: string;
    text: string;
    completed: boolean;
}

export interface Task {
    id: string;
    title: string;
    description: string;
    date: string;
    startTime: string;
    subTasks: SubTask[];
    completed: boolean;
}

export interface WeekDay {
    readonly name: string;
    readonly date: Date;
    readonly dateString: string;
}

export interface FormData {
    title: string;
    description: string;
    date: string;
    startTime: string;
    subTasks: SubTask[];
}