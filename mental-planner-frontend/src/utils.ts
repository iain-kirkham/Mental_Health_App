import { format, startOfWeek, addDays } from 'date-fns';
import { Task, SubTask, WeekDay, FormData } from '@/types';

export const getDateString = (date: Date): string => format(date, 'yyyy-MM-dd');

export const getCurrentMonday = (): Date => startOfWeek(new Date(), { weekStartsOn: 1 });

export const addDaysToDate = (date: Date, days: number): Date => addDays(date, days);

const dayNames = [
    'Sunday',
    'Monday',
    'Tuesday',
    'Wednesday',
    'Thursday',
    'Friday',
    'Saturday',
];

export const getWeekDays = (currentWeek: Date): WeekDay[] => {
    const monday = startOfWeek(currentWeek, { weekStartsOn: 1 });
    return Array.from({ length: 7 }).map((_, i) => {
        const date = addDays(monday, i);
        return {
            name: dayNames[date.getDay()],
            date,
            dateString: getDateString(date),
        };
    });
};

export const getInitialTasks = (): Task[] => {
    const monday = getCurrentMonday();

    return [
        {
            id: '1',
            title: 'Project Planning',
            description: 'Outline quarterly goals and milestones',
            date: getDateString(monday),
            startTime: '09:00',
            completed: false,
            subTasks: [
                { id: '1-1', text: 'Review previous quarter', completed: true },
                { id: '1-2', text: 'Define new objectives', completed: false },
            ],
        },
        {
            id: '2',
            title: 'Team Meeting',
            description: 'Weekly sync with engineering team',
            date: getDateString(addDaysToDate(monday, 2)),
            startTime: '14:00',
            completed: false,
            subTasks: [],
        },
        {
            id: '3',
            title: 'Documentation',
            description: 'Update API documentation',
            date: getDateString(addDaysToDate(monday, 4)),
            startTime: '10:00',
            completed: true,
            subTasks: [
                { id: '3-1', text: 'Review current docs', completed: true },
                { id: '3-2', text: 'Update endpoints', completed: true },
                { id: '3-3', text: 'Add examples', completed: true },
            ],
        },
    ];
};

export const emptyFormData: FormData = {
    title: '',
    description: '',
    date: getDateString(new Date()),
    startTime: '',
    subTasks: [],
};
