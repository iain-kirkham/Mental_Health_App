'use client'

import React from 'react';
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import { TaskCard } from "./TaskCard";

/**
 * DayColumn component represents a single day in the weekly view
 */
export function DayColumn({
                              day,
                              tasks,
                              onNewTask,
                              onEdit,
                              onDelete,
                              onTaskCompletion
                          }) {
    // Filter and sort tasks for this day
    const dayTasks = tasks
        .filter(task => task.date === day.dateString)
        .sort((a, b) => {
            if (!a.startTime) return 1;
            if (!b.startTime) return -1;
            return a.startTime.localeCompare(b.startTime);
        });

    const handleNewTask = () => onNewTask(day.dateString);

    return (
        <div className="border-r flex flex-col h-full">
            {/* Day header */}
            <div className="p-2 border-b bg-gray-100 sticky top-0 z-10">
                <div className="text-sm font-medium">{day.name}</div>
                <div className="text-xs text-gray-500">
                    {day.date.toLocaleDateString('en-GB', { month: 'short', day: 'numeric' })}
                </div>
            </div>

            <div className="p-2 border-b bg-gray-50 flex justify-between items-center">
                <Button
                    variant="ghost"
                    className="flex items-center justify-center text-gray-600 text-sm hover:bg-gray-100"
                    onClick={handleNewTask}
                >
                    <Plus size={16} className="mr-1" /> Add task
                </Button>
            </div>

            {/* Task container */}
            <div className="flex-grow overflow-y-auto p-2">
                {dayTasks.map(task => (
                    <TaskCard
                        key={task.id}
                        task={task}
                        onEdit={onEdit}
                        onDelete={onDelete}
                        onTaskCompletion={onTaskCompletion}
                    />
                ))}
            </div>
        </div>
    );
}