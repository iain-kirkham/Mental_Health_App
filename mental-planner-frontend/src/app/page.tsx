import Link from 'next/link';
import { CalendarDays, LineChart, Timer } from 'lucide-react';
import PageInset from '@/components/PageInset';

const tools = [
  {
    href: '/planner',
    icon: CalendarDays,
    name: 'Planner',
    description: 'Lay tasks across the week, drag them where they fit, break them into subtasks.',
    cta: 'Open planner',
  },
  {
    href: '/focus',
    icon: Timer,
    name: 'Focus sessions',
    description: 'Run a focus timer against a task, or just start one and see where it goes.',
    cta: 'Start a session',
  },
  {
    href: '/mood-tracker',
    icon: LineChart,
    name: 'Mood tracker',
    description: "Log how you're doing and what's behind it, then watch the pattern over weeks.",
    cta: 'Log your mood',
  },
];

export default function Home() {
  return (
    <main className="min-h-[calc(100vh-4rem)] bg-background py-16 sm:py-20">
      <PageInset size="wide">
        <div className="max-w-xl">
          <h1 className="text-3xl font-semibold leading-tight tracking-tight text-foreground sm:text-4xl">
            A calmer way to get through the day.
          </h1>
          <p className="mt-3 text-base leading-relaxed text-muted-foreground">
            Built for brains that don&apos;t do rigid time-blocking - flexible tools instead of a schedule that falls apart the moment something interrupts it.
          </p>
        </div>

        <div className="mt-14 divide-y divide-border border-t border-border">
          {tools.map(({ href, icon: Icon, name, description, cta }) => (
            <Link
              key={href}
              href={href}
              className="group -mx-3 flex items-center gap-5 rounded-md px-3 py-6 transition-colors hover:bg-muted/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring sm:gap-6"
            >
              <Icon className="h-5 w-5 shrink-0 text-muted-foreground" aria-hidden />
              <div className="min-w-0 flex-1">
                <h2 className="text-base font-medium text-foreground">{name}</h2>
                <p className="mt-0.5 max-w-md text-sm text-muted-foreground">{description}</p>
              </div>
              <span className="hidden shrink-0 text-sm font-medium text-primary sm:block">{cta}</span>
            </Link>
          ))}
        </div>

        <p className="mt-16 border-t border-border pt-6 text-sm text-muted-foreground/80">
          Made by one ADHD brain, for others like it.
        </p>
      </PageInset>
    </main>
  );
}
