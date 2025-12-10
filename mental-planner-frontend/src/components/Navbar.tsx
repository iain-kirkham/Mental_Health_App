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
import { SignInButton, SignOutButton, useUser } from '@clerk/nextjs'
import { Timer, LineChart, User, Home as HomeIcon, Menu, X } from 'lucide-react'
import React, { useState } from 'react'
import PageInset from '@/components/PageInset'

export function Navbar() {
    const { isSignedIn, user } = useUser()
    const pathname = usePathname() || '/'
    const [mobileOpen, setMobileOpen] = useState(false)

    const routes = [
        {
            href: '/',
            label: 'Home',
            icon: <HomeIcon className="mr-2 h-4 w-4" />
        },
        {
            href: '/pomodoro',
            label: 'Pomodoro',
            icon: <Timer className="mr-2 h-4 w-4" />
        },
        {
            href: '/mood-tracker',
            label: 'Mood Tracker',
            icon: <LineChart className="mr-2 h-4 w-4" />
        },
    ]

    return (
        <header className="w-full relative bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800">
            <div className="w-full">
                <PageInset size="wide">
                    <div className="flex items-center h-16">
                        <Link href="/" className="flex items-center gap-3" aria-label="Go to home">
                            <span className="font-medium text-slate-800 dark:text-slate-100">ADHD focus tool</span>
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
                                                            ? "bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-slate-100"
                                                            : "text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800"
                                                    )}
                                                    aria-current={isActive ? 'page' : undefined}
                                                >
                                                    {route.icon}
                                                    <span className="hidden sm:inline">{route.label}</span>
                                                </Link>
                                            </NavigationMenuItem>
                                        )
                                    })}
                                </NavigationMenuList>
                            </NavigationMenu>
                        </nav>

                        {/* User area - desktop shown as before; on mobile it's duplicated inside the slide-down menu */}
                        <div className="hidden md:flex md:ml-auto items-center space-x-3">
                            {isSignedIn ? (
                                <>
                                    <div className="hidden sm:flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-100">
                                        <User className="h-4 w-4 opacity-90" />
                                        <span>{user?.firstName || user?.primaryEmailAddress?.emailAddress?.split('@')[0]}</span>
                                    </div>
                                    <SignOutButton>
                                        <Button variant="outline" size="sm" className="text-slate-700 dark:text-slate-100 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800">
                                            Sign Out
                                        </Button>
                                    </SignOutButton>
                                </>
                            ) : (
                                <SignInButton>
                                    <Button size="sm" className="bg-slate-800 text-white hover:bg-slate-900 dark:bg-slate-700 dark:hover:bg-slate-600">
                                        Sign In
                                    </Button>
                                </SignInButton>
                            )}
                        </div>

                        {/* Mobile hamburger button (right side) */}
                        <div className="ml-auto md:hidden">
                            <button
                                aria-label={mobileOpen ? 'Close menu' : 'Open menu'}
                                aria-expanded={mobileOpen}
                                onClick={() => setMobileOpen(!mobileOpen)}
                                className="inline-flex items-center justify-center p-2 rounded-md text-slate-700 dark:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-300"
                            >
                                {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
                            </button>
                        </div>
                    </div>
                </PageInset>
            </div>

            {/* Mobile dropdown as absolute child of the header (no portal) */}
            {mobileOpen && (
                <div className="absolute top-full left-0 right-0 w-full bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 z-50 md:hidden overflow-auto max-h-[80vh]">
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
                                                ? "bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-slate-100"
                                                : "text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800"
                                        )}
                                    >
                                        {route.icon}
                                        <span className="ml-1">{route.label}</span>
                                    </Link>
                                )
                            })}

                            <div className="border-t border-slate-100 dark:border-slate-800 mt-3 pt-3 px-0">
                                {isSignedIn ? (
                                    <div className="flex flex-col gap-2 px-0">
                                        <div className="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-100">
                                            <User className="h-4 w-4 opacity-90" />
                                            <span>{user?.firstName || user?.primaryEmailAddress?.emailAddress?.split('@')[0]}</span>
                                        </div>
                                        <SignOutButton>
                                            <Button variant="outline" size="sm" className="text-slate-700 dark:text-slate-100 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800">
                                                Sign Out
                                            </Button>
                                        </SignOutButton>
                                    </div>
                                ) : (
                                    <SignInButton>
                                        <Button size="sm" className="w-full bg-slate-800 text-white hover:bg-slate-900 dark:bg-slate-700 dark:hover:bg-slate-600">
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
