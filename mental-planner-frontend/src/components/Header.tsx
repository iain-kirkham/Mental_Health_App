'use client'

import React from 'react';
import { Button } from "@/components/ui/button";
import { ArrowLeft, ArrowRight } from "lucide-react";

/**
 * Header component with week navigation controls
 */
export function Header({ daysOfWeek, onNavigate, onReset }) {
    return (
        <div className="flex justify-between items-center p-4 border-b bg-white">
            <h1 className="text-2xl font-bold text-gray-800">Weekly Planner</h1>

            <div className="flex space-x-4 items-center">
                <div className="flex items-center space-x-2">
                    <Button variant="outline" size="sm" onClick={() => onNavigate('prev')}>
                        <ArrowLeft size={16} />
                    </Button>

                    <span className="text-sm font-medium">
            {daysOfWeek[0].date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} - {daysOfWeek[6].date.toLocaleDateString('en-GB', { month: 'short', day: 'numeric' })}
          </span>

                    <Button variant="outline" size="sm" onClick={() => onNavigate('next')}>
                        <ArrowRight size={16} />
                    </Button>
                </div>

                <Button variant="default" size="sm" onClick={onReset}>
                    Today
                </Button>
            </div>
        </div>
    );
}