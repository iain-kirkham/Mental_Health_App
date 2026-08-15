'use client'

import React from 'react';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import MoodHistory from '@/components/mood/MoodHistory';
import PageHeader from '@/components/PageHeader';

export default function MoodHistoryPage() {
    return (
        <>
            <PageHeader title="📓 Your journal">
                <Link href="/mood-tracker">
                    <Button variant="ghost" size="sm">
                        <ArrowLeft className="mr-2 h-4 w-4" />
                        Back to mood tracker
                    </Button>
                </Link>
            </PageHeader>

            <div className="px-3 py-3 md:px-4">
                <MoodHistory />
            </div>
        </>
    );
}
