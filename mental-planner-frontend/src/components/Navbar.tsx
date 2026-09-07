'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { cn } from '@/lib/utils'
import {
    NavigationMenu,
    NavigationMenuItem,
    NavigationMenuList,
    navigationMenuTriggerStyle,
} from '@/components/ui/navigation-menu'
import { Button } from '@/components/ui/button'
import { ModeToggle } from '@/components/mode-toggle'
import { SignInButton, useClerk, useUser } from '@clerk/nextjs'
import { Timer, LineChart, User, Home as HomeIcon, CalendarDays, Menu, X } from 'lucide-react'
import React, { useState } from 'react'
import PageInset from '@/components/PageInset'
import { useTimerStore } from '@/store/timerStore'
import { formatTime } from '@/lib/focus-format'
import { useQueryClient } from '@tanstack/react-query'

/**
 * Live countdown shown beside its nav link. It is the only part of the navbar
 * subscribed to the timer, so a tick re-renders this span rather than the whole
 * header (Radix menu, Clerk user area and all). Scoped to focus mode only -
 * a plain task stopwatch running elsewhere shouldn't surface a Focus badge here.
 */
function FocusCountdown() {
    const mode = useTimerStore((state) => state.mode)
    const isRunning = useTimerStore((state) => state.isRunning)
    const elapsedSeconds = useTimerStore((state) => state.elapsedSeconds)
    const sessionLengthMinutes = useTimerStore((state) => state.sessionLengthMinutes)

    if (mode !== 'focus' || !isRunning || sessionLengthMinutes === null) return null
    const timeLeft = Math.max(0, sessionLengthMinutes * 60 - elapsedSeconds)

    return (
        <span
            className="ml-2 rounded-full bg-chart-2/15 text-chart-2 text-xs font-mono font-semibold px-2 py-0.5 tabular-nums"
            aria-label={`Focus session running, ${formatTime(timeLeft)} remaining`}
        >
            {formatTime(timeLeft)}
        </span>
    )
}

/**
 * Nav links, with an optional badge slot so a route can show live status without
 * the renderers having to know which href it belongs to.
 */
const routes: { href: string; label: string; icon: React.ReactNode; badge?: React.ReactNode }[] = [
    {
        href: '/',
        label: 'Home',
        icon: <HomeIcon className="mr-2 h-4 w-4" />
    },
    {
        href: '/planner',
        label: 'Planner',
        icon: <CalendarDays className="mr-2 h-4 w-4" />
    },
    {
        href: '/focus',
        label: 'Focus',
        icon: <Timer className="mr-2 h-4 w-4" />,
        badge: <FocusCountdown />
    },
    {
        href: '/mood-tracker',
        label: 'Mood Tracker',
        icon: <LineChart className="mr-2 h-4 w-4" />
    },
]

export function Navbar() {
    const { isSignedIn, user } = useUser()
    const { signOut } = useClerk()
    const queryClient = useQueryClient()
    const pathname = usePathname() || '/'
    const [mobileOpen, setMobileOpen] = useState(false)

    // Task data is cached to localStorage (see QueryProvider) so it survives reloads while
    // offline. That cache isn't scoped per-user, so it must be wiped on sign-out - otherwise
    // the next person to use this browser/profile could read the previous user's tasks.
    const handleSignOut = () => {
        void signOut(() => {
            queryClient.clear()
            window.localStorage.removeItem('mha-query-cache')
        })
    }

    return (
        <header className="w-full relative bg-background border-b border-border">
            <div className="w-full">
                <PageInset size="wide">
                    <div className="flex items-center h-16">
                        <Link href="/" className="flex items-center gap-3" aria-label="Go to home">
                            <span className="font-medium text-foreground">ADHD focus tool</span>
                        </Link>

                        {/* Navigation - desktop only (center) */}
                        <nav className="hidden md:flex flex-1 md:mx-4">
                            <NavigationMenu>
                                <NavigationMenuList className="flex items-center gap-2 justify-center">
                                    {routes.map((route) => {
                                        const isActive = route.href === '/' ? pathname === '/' : pathname.startsWith(route.href)
                                        return (
                                            <NavigationMenuItem key={route.href}>
                                                <Link
                                                    href={route.href}
                                                    className={cn(
                                                        navigationMenuTriggerStyle(),
                                                        "flex items-center px-3 py-2 rounded-md transition-colors duration-150",
                                                        isActive
                                                            ? "bg-accent text-accent-foreground"
                                                            : "text-foreground/80 hover:bg-accent hover:text-accent-foreground"
                                                    )}
                                                    aria-current={isActive ? 'page' : undefined}
                                                >
                                                    {route.icon}
                                                    <span className="hidden sm:inline">{route.label}</span>
                                                    {route.badge}
                                                </Link>
                                            </NavigationMenuItem>
                                        )
                                    })}
                                </NavigationMenuList>
                            </NavigationMenu>
                        </nav>

                        {/* User area - desktop shown as before; on mobile it's duplicated inside the slide-down menu */}
                        <div className="hidden md:flex md:ml-auto items-center space-x-3">
                            <ModeToggle />
                            {isSignedIn ? (
                                <>
                                    <div className="hidden sm:flex items-center gap-2 text-sm font-medium text-foreground">
                                        <User className="h-4 w-4 opacity-90" />
                                        <span>{user?.firstName || user?.primaryEmailAddress?.emailAddress?.split('@')[0]}</span>
                                    </div>
                                    <Button variant="outline" size="sm" onClick={handleSignOut}>
                                        Sign Out
                                    </Button>
                                </>
                            ) : (
                                <SignInButton>
                                    <Button size="sm">
                                        Sign In
                                    </Button>
                                </SignInButton>
                            )}
                        </div>

                        {/* Mobile hamburger button (right side) */}
                        <div className="ml-auto flex items-center gap-2 md:hidden">
                            <ModeToggle />
                            <button
                                aria-label={mobileOpen ? 'Close menu' : 'Open menu'}
                                aria-expanded={mobileOpen}
                                onClick={() => setMobileOpen(!mobileOpen)}
                                className="inline-flex items-center justify-center p-2 rounded-md text-foreground hover:bg-accent hover:text-accent-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                            >
                                {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
                            </button>
                        </div>
                    </div>
                </PageInset>
            </div>

            {/* Mobile dropdown as absolute child of the header (no portal) */}
            {mobileOpen && (
                <div className="absolute top-full left-0 right-0 w-full bg-background border-t border-border z-50 md:hidden overflow-auto max-h-[80vh]">
                    <div className="w-full py-4">
                        <div className="flex flex-col gap-2 px-6">
                            {routes.map(route => {
                                const isActive = route.href === '/' ? pathname === '/' : pathname.startsWith(route.href)
                                return (
                                    <Link
                                        key={route.href}
                                        href={route.href}
                                        onClick={() => setMobileOpen(false)}
                                        className={cn(
                                            "flex items-center px-3 py-2 rounded-md transition-colors duration-150 w-full",
                                            isActive
                                                ? "bg-accent text-accent-foreground"
                                                : "text-foreground/80 hover:bg-accent hover:text-accent-foreground"
                                        )}
                                    >
                                        {route.icon}
                                        <span className="ml-1">{route.label}</span>
                                        {route.badge}
                                    </Link>
                                )
                            })}

                            <div className="border-t border-border mt-3 pt-3 px-0">
                                {isSignedIn ? (
                                    <div className="flex flex-col gap-2 px-0">
                                        <div className="flex items-center gap-2 text-sm font-medium text-foreground">
                                            <User className="h-4 w-4 opacity-90" />
                                            <span>{user?.firstName || user?.primaryEmailAddress?.emailAddress?.split('@')[0]}</span>
                                        </div>
                                        <Button variant="outline" size="sm" onClick={handleSignOut}>
                                            Sign Out
                                        </Button>
                                    </div>
                                ) : (
                                    <SignInButton>
                                        <Button size="sm" className="w-full">
                                            Sign In
                                        </Button>
                                    </SignInButton>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </header>
    )
}
