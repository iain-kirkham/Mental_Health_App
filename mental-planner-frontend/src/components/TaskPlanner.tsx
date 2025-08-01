'use client'

import React, { useState } from 'react';
import { Header } from '@/components/Header';
import { WeeklyGrid } from '@/components/WeeklyGrid';
import { TaskDialog } from '@/components/TaskDialog';
import { getWeekDays, getInitialTasks, emptyFormData } from '@/utils';
import { Task, SubTask, WeekDay, FormData } from '@/types';

/**
 * MinimalTaskPlanner - Main part for the weekly task planning app
 */
export default function TaskPlanner() {
    const [tasks, setTasks] = useState<Task[]>(getInitialTasks());
    const [currentWeek, setCurrentWeek] = useState<Date>(new Date());
    const [isDialogOpen, setIsDialogOpen] = useState<boolean>(false);
    const [currentTask, setCurrentTask] = useState<Task | null>(null);
    const [formData, setFormData] = useState<Partial<Task>>(emptyFormData);

    // Get the days for the current week
    const daysOfWeek = getWeekDays(currentWeek);

    // Week navigation
    const navigateWeek = (direction: 'next' | 'prev') => { // Type 'direction'
        const newDate = new Date(currentWeek);
        newDate.setDate(newDate.getDate() + (direction === 'next' ? 7 : -7));
        setCurrentWeek(newDate);
    };

    // Reset to the current week
    const resetToCurrentWeek = () => {
        setCurrentWeek(new Date());
    };

    // Task handlers
    const handleEdit = (task: Task) => {
        setCurrentTask(task);
        setFormData({
            title: task.title,
            description: task.description || '',
            date: task.date,
            startTime: task.startTime || '',
            subTasks: [...task.subTasks]
        });
        setIsDialogOpen(true);
    };

    const handleNewTask = (dateString: string) => {
        setCurrentTask(null);
        setFormData({
            ...emptyFormData,
            date: dateString
        });
        setIsDialogOpen(true);
    };

    const handleSaveTask = () => {
        if (!formData.title || formData.title.trim() === '') {
            console.warn("Task title cannot be empty.");
            return;
        }

        if (currentTask) {
            // Update existing task
            setTasks(prevTasks =>
                prevTasks.map(task =>
                    task.id === currentTask.id // 'task' is now Task, 'currentTask' is Task
                        ? { ...task, ...formData as Task }
                        : task
                )
            );
        } else {
            // Create new task
            // Ensure all required Task properties are present for a new task
            const newTask: Task = {
                id: Date.now().toString(),
                title: formData.title,
                description: formData.description || '', // Default if undefined
                date: formData.date || '', // Default if undefined
                startTime: formData.startTime || '', // Default if undefined
                subTasks: formData.subTasks || [], // Default if undefined
                completed: false
            };
            setTasks(prevTasks => [...prevTasks, newTask]);
        }

        setIsDialogOpen(false);
        // Reset form data after saving
        setFormData(emptyFormData);
        setCurrentTask(null); // Clear the current task after save
    };

    const handleDeleteTask = (taskId: string) => { // Type 'taskId'
        setTasks(prevTasks => prevTasks.filter(task => task.id !== taskId));
    };

    const handleTaskCompletion = (taskId: string, completed: boolean) => { // Type parameters
        setTasks(prevTasks =>
            prevTasks.map(task =>
                task.id === taskId
                    ? { ...task, completed }
                    : task
            )
        );
    };

    const handleCloseDialog = () => {
        setIsDialogOpen(false);
        // Clear the current task and form data when closing the dialog without saving
        setCurrentTask(null);
        setFormData(emptyFormData);
    };

    return (
        <div className="flex flex-col h-screen max-h-screen bg-gray-50">
            {/* Header with controls */}
            <Header
                daysOfWeek={daysOfWeek}
                onNavigate={navigateWeek}
                onReset={resetToCurrentWeek}
            />

            {/* Calendar Grid */}
            <WeeklyGrid
                daysOfWeek={daysOfWeek}
                tasks={tasks}
                onNewTask={handleNewTask}
                onEdit={handleEdit}
                onDelete={handleDeleteTask}
                onTaskCompletion={handleTaskCompletion}
            />

            {/* Task Edit/Create Dialog */}
            <TaskDialog
                isOpen={isDialogOpen}
                onClose={handleCloseDialog}
                formData={formData}
                setFormData={setFormData}
                onSave={handleSaveTask}
                daysOfWeek={daysOfWeek}
                isEditing={!!currentTask}
            />
        </div>
    );
}