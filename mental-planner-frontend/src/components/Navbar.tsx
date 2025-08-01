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
import { Timer, Calendar, LineChart, User } from 'lucide-react'

export function Navbar() {
    const { isSignedIn, user } = useUser()
    const pathname = usePathname()

    const routes = [
        {
            href: '/pomodoro',
            label: 'Pomodoro Timer',
            icon: <Timer className="mr-2 h-4 w-4" />
        },
        {
            href: '/planner',
            label: 'Planner',
            icon: <Calendar className="mr-2 h-4 w-4" />
        },
        {
            href: '/mood-tracker',
            label: 'Mood Tracker',
            icon: <LineChart className="mr-2 h-4 w-4" />
        },
    ]

    return (
        <div className="border-b">
            <div className="flex h-16 items-center px-4 container mx-auto">
                <NavigationMenu className="mx-auto">
                    <NavigationMenuList>
                        {routes.map((route) => (
                            <NavigationMenuItem key={route.href}>
                                <Link
                                    href={route.href}
                                    className={cn(
                                        navigationMenuTriggerStyle(),
                                        pathname === route.href && "bg-accent text-accent-foreground",
                                        "flex items-center"
                                    )}
                                >
                                    {route.icon}
                                    {route.label}
                                </Link>
                            </NavigationMenuItem>
                        ))}
                    </NavigationMenuList>
                </NavigationMenu>

                <div className="ml-auto flex items-center space-x-4">
                    {isSignedIn ? (
                        <>
                            <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
                                <User className="h-4 w-4" />
                                {user?.firstName || user?.primaryEmailAddress?.emailAddress?.split('@')[0]}
                            </div>
                            <SignOutButton>
                                <Button variant="outline" size="sm">
                                    Sign Out
                                </Button>
                            </SignOutButton>
                        </>
                    ) : (
                        <SignInButton>
                            <Button size="sm">Sign In</Button>
                        </SignInButton>
                    )}
                </div>
            </div>
        </div>
    )
}