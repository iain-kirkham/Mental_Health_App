'use client'

import React from 'react';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import PomodoroHistory from '@/components/pomodoro/PomodoroHistory';
import PageHeader from '@/components/PageHeader';

export default function PomodoroHistoryPage() {
    return (
        <>
            <PageHeader title="Session history">
                <Link href="/pomodoro">
                    <Button variant="ghost" size="sm">
                        <ArrowLeft className="mr-2 h-4 w-4" />
                        Back to timer
                    </Button>
                </Link>
            </PageHeader>

            <div className="px-3 py-3 md:px-4">
                <PomodoroHistory />
            </div>
        </>
    );
}
