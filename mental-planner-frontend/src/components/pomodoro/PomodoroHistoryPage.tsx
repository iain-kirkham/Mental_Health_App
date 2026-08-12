'use client'

import React from 'react';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import PageInset from '@/components/PageInset';
import { Button } from '@/components/ui/button';
import PomodoroHistory from '@/components/pomodoro/PomodoroHistory';

export default function PomodoroHistoryPage() {
    return (
        <>
            <PageHeader title={<>🍅 Session history</>} subtitle={<>Look back on past focus sessions</>} size="wide" />
            <div className="pt-4 md:pt-6" />

            <PageInset size="wide" className="pb-2">
                <Link href="/pomodoro">
                    <Button variant="outline" size="sm">
                        <ArrowLeft className="mr-2 h-4 w-4" />
                        Back to timer
                    </Button>
                </Link>
            </PageInset>

            <PageInset size="wide" className="pb-8 md:pb-10 pt-4">
                <PomodoroHistory />
            </PageInset>
        </>
    );
}
