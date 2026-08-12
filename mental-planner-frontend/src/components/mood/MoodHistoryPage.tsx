'use client'

import React from 'react';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import PageInset from '@/components/PageInset';
import { Button } from '@/components/ui/button';
import MoodHistory from '@/components/mood/MoodHistory';

export default function MoodHistoryPage() {
    return (
        <>
            <PageHeader title={<>📓 Your journal</>} subtitle={<>Look back on past mood entries</>} size="wide" />
            <div className="pt-4 md:pt-6" />

            <PageInset size="wide" className="pb-2">
                <Link href="/mood-tracker">
                    <Button variant="outline" size="sm">
                        <ArrowLeft className="mr-2 h-4 w-4" />
                        Back to mood tracker
                    </Button>
                </Link>
            </PageInset>

            <PageInset size="wide" className="pb-8 md:pb-10 pt-4">
                <MoodHistory />
            </PageInset>
        </>
    );
}
