import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Timer, LineChart, Brain } from 'lucide-react';

export default function Home() {
  return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-background p-4">
        <div className="container max-w-4xl mx-auto space-y-8">
          <div className="text-center space-y-4">
            <h1 className="text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl">
              ADHD Focus Companion
            </h1>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              A mental health toolkit designed <span className="font-semibold">with ADHD in mind</span>.
              No rigid time-blocking. Just tools that work with your brain.
            </p>
          </div>

          <div className="grid gap-6 md:grid-cols-2 mt-12">
            <Link href="/pomodoro">
              <Card className="hover:border-primary transition-colors cursor-pointer h-full">
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <Timer className="h-6 w-6 text-primary" />
                    <CardTitle>Pomodoro Timer</CardTitle>
                  </div>
                  <CardDescription>
                    Manage hyperfocus sessions with customizable timers. Track and rate your productivity patterns.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <Button variant="outline" className="w-full">Start Timer</Button>
                </CardContent>
              </Card>
            </Link>

            <Link href="/mood-tracker">
              <Card className="hover:border-primary transition-colors cursor-pointer h-full">
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <LineChart className="h-6 w-6 text-primary" />
                    <CardTitle>Mood Tracker</CardTitle>
                  </div>
                  <CardDescription>
                    Log daily moods with customizable factors. Identify patterns and triggers over time.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <Button variant="outline" className="w-full">Track Mood</Button>
                </CardContent>
              </Card>
            </Link>
          </div>

          <div className="mt-12 text-center">
            <Card className="bg-muted/50">
              <CardHeader>
                <div className="flex items-center justify-center gap-2 mb-2">
                  <Brain className="h-6 w-6" />
                  <CardTitle>Built for ADHD Brains</CardTitle>
                </div>
                <CardDescription className="text-base">
                  This app is designed by an ADHDer for ADHDers. It focuses on flexibility and understanding rather than rigid structures, helping you work with your brain and not against it.
                </CardDescription>
              </CardHeader>
            </Card>
          </div>
        </div>
      </main>
  );
}
