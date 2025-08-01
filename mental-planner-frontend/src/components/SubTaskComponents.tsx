'use client'

import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import { Trash2, Plus } from "lucide-react";
import { cn } from "@/lib/utils";

/**
 * Individual subtask item
 */
export function SubTaskItem({ subtask, onToggle, onDelete }) {
    return (
        <div className="flex items-center space-x-2 bg-gray-50 p-2 rounded-md">
            <Checkbox
                checked={subtask.completed}
                onCheckedChange={() => onToggle(subtask.id)}
            />
            <span className={cn(
                "text-sm flex-grow",
                subtask.completed ? "line-through text-gray-500" : ""
            )}>
        {subtask.text}
      </span>
            <Button
                variant="ghost"
                size="icon"
                className="h-5 w-5"
                onClick={() => onDelete(subtask.id)}
            >
                <Trash2 size={12} />
            </Button>
        </div>
    );
}

/**
 * Component for managing subtasks (add, list, etc.)
 */
export function SubTasksSection({ subtasks, onUpdate }) {
    const [newSubTask, setNewSubTask] = useState("");

    const handleAddSubTask = () => {
        if (newSubTask.trim() === "") return;

        const newSubTaskObj = {
            id: `${Date.now()}`,
            text: newSubTask,
            completed: false
        };

        onUpdate([...subtasks, newSubTaskObj]);
        setNewSubTask("");
    };

    const handleDeleteSubTask = (subTaskId) => {
        onUpdate(subtasks.filter(st => st.id !== subTaskId));
    };

    const handleSubTaskCompletion = (subTaskId) => {
        onUpdate(subtasks.map(st =>
            st.id === subTaskId
                ? { ...st, completed: !st.completed }
                : st
        ));
    };

    const handleInputKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleAddSubTask();
        }
    };

    return (
        <div>
            <label className="text-sm font-medium mb-2 block">Subtasks</label>

            <div className="space-y-2 max-h-32 overflow-y-auto mb-2">
                {subtasks.map(subtask => (
                    <SubTaskItem
                        key={subtask.id}
                        subtask={subtask}
                        onToggle={handleSubTaskCompletion}
                        onDelete={handleDeleteSubTask}
                    />
                ))}
            </div>

            <div className="flex space-x-2">
                <Input
                    value={newSubTask}
                    onChange={e => setNewSubTask(e.target.value)}
                    placeholder="Add subtask"
                    onKeyDown={handleInputKeyDown}
                />
                <Button type="button" onClick={handleAddSubTask}>
                    <Plus size={16} />
                </Button>
            </div>
        </div>
    );
}