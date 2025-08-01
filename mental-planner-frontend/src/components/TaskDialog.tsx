'use client'

import React from 'react';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { SubTasksSection } from "./SubTaskComponents";

/**
 * Dialog for creating or editing tasks
 */
export function TaskDialog({
                               isOpen,
                               onClose,
                               formData,
                               setFormData,
                               onSave,
                               daysOfWeek,
                               isEditing
                           }) {
    const handleSubTasksUpdate = (updatedSubTasks) => {
        setFormData(prev => ({
            ...prev,
            subTasks: updatedSubTasks
        }));
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>
                        {isEditing ? 'Edit Task' : 'Create New Task'}
                    </DialogTitle>
                </DialogHeader>

                <div className="space-y-4 py-2">
                    <div>
                        <label className="text-sm font-medium">Title</label>
                        <Input
                            value={formData.title}
                            onChange={e => setFormData({...formData, title: e.target.value})}
                            placeholder="Task title"
                        />
                    </div>

                    <div>
                        <label className="text-sm font-medium">Description</label>
                        <Textarea
                            value={formData.description}
                            onChange={e => setFormData({...formData, description: e.target.value})}
                            placeholder="Task description"
                            rows={3}
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div>
                            <label className="text-sm font-medium">Day</label>
                            <Select
                                value={formData.date}
                                onValueChange={(val) => setFormData({...formData, date: val})}
                            >
                                <SelectTrigger>
                                    <SelectValue placeholder="Select day" />
                                </SelectTrigger>
                                <SelectContent>
                                    {daysOfWeek.map(day => (
                                        <SelectItem key={day.dateString} value={day.dateString}>
                                            {day.name} ({day.date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })})
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>

                        <div>
                            <label className="text-sm font-medium">Start Time</label>
                            <Input
                                type="time"
                                value={formData.startTime || ''}
                                onChange={e => setFormData({...formData, startTime: e.target.value})}
                                placeholder="Start time"
                            />
                        </div>
                    </div>

                    <SubTasksSection
                        subtasks={formData.subTasks}
                        onUpdate={handleSubTasksUpdate}
                    />
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>
                        Cancel
                    </Button>
                    <Button onClick={onSave}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}