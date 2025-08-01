'use client'

import React from 'react';
import { DayColumn } from './DayColumn';

/**
 * WeeklyGrid component renders the 7-day calendar grid
 */
export function WeeklyGrid({
                               daysOfWeek,
                               tasks,
                               onNewTask,
                               onEdit,
                               onDelete,
                               onTaskCompletion
                           }) {
    return (
        <div className="flex-grow overflow-auto">
            <div className="grid grid-cols-7 h-full">
                {daysOfWeek.map(day => (
                    <DayColumn
                        key={day.dateString}
                        day={day}
                        tasks={tasks}
                        onNewTask={onNewTask}
                        onEdit={onEdit}
                        onDelete={onDelete}
                        onTaskCompletion={onTaskCompletion}
                    />
                ))}
            </div>
        </div>
    );
}