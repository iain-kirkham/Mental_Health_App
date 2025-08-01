'use client'

import React from 'react';
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
    CheckCircle2,
    Circle,
    X,
    AlignJustify,
    Pencil
} from "lucide-react";
import { Card } from "@/components/ui/card";

/**
 * TaskCard component displays individual task items
 */
export function TaskCard({ task, onEdit, onDelete, onTaskCompletion }) {
    const completedSubTasks = task.subTasks.filter(st => st.completed).length;
    const totalSubTasks = task.subTasks.length;

    const handleEdit = (e) => {
        e.stopPropagation();
        onEdit(task);
    };

    const handleDelete = (e) => {
        e.stopPropagation();
        onDelete(task.id);
    };

    const handleCompletion = (e) => {
        e.stopPropagation();
        onTaskCompletion(task.id, !task.completed);
    };

    return (
        <Card
            className={cn(
                "w-full mb-2 rounded-md p-3 hover:ring-2 hover:ring-primary transition-all shadow-sm",
                task.completed ? "bg-green-50 border-green-200" : "bg-white"
            )}
        >
            <div className="flex justify-between items-start h-full flex-col">
                <div className="w-full">
                    <div className="flex justify-between w-full">
                        <h3 className={cn(
                            "font-medium text-sm truncate max-w-[80%]",
                            task.completed ? "line-through text-green-700" : ""
                        )}>
                            {task.title}
                        </h3>
                        <div className="flex space-x-1">
                            <Button variant="ghost" size="icon" className="h-5 w-5" onClick={handleEdit}>
                                <Pencil size={12} />
                            </Button>
                            <Button variant="ghost" size="icon" className="h-5 w-5" onClick={handleDelete}>
                                <X size={12} />
                            </Button>
                        </div>
                    </div>
                    {task.startTime && (
                        <p className="text-xs text-gray-500 mt-1">{task.startTime}</p>
                    )}
                    {task.description && (
                        <p className="text-xs text-gray-600 mt-1 line-clamp-2">{task.description}</p>
                    )}
                </div>

                <div className="w-full mt-2">
                    {task.subTasks.length > 0 && (
                        <div className="mt-1 text-xs text-gray-600">
              <span className="flex items-center">
                <AlignJustify size={10} className="mr-1" />
                  {completedSubTasks}/{totalSubTasks} subtasks
              </span>
                        </div>
                    )}
                    <div className="flex justify-between items-center mt-1">
                        <Button
                            variant="ghost"
                            size="sm"
                            className="h-6 p-0"
                            onClick={handleCompletion}
                        >
                            {task.completed ?
                                <CheckCircle2 size={16} className="text-green-600" /> :
                                <Circle size={16} />
                            }
                        </Button>
                        <Badge variant={task.completed ? "outline" : "secondary"} className="text-xs h-5">
                            {task.completed ? "Completed" : "In Progress"}
                        </Badge>
                    </div>
                </div>
            </div>
        </Card>
    );
}