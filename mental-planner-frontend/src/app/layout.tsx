import type { Metadata } from "next";
import localFont from "next/font/local";
import "./globals.css";
import React from "react";
import { Toaster } from "sonner";
import {
  ClerkProvider
} from '@clerk/nextjs'

import { Navbar } from '@/components/Navbar'
import { ThemeProvider } from '@/components/theme-provider'
import { PomodoroSessionProvider } from '@/contexts/PomodoroSessionContext'
import { GlobalPomodoroModal } from '@/components/GlobalPomodoroModal'
import { TimerStoreBridge } from '@/components/TimerStoreBridge'
import { QueryProvider } from '@/components/QueryProvider'

const geistSans = localFont({
  src: "./fonts/GeistVF.woff",
  variable: "--font-geist-sans",
  weight: "100 900",
});
const geistMono = localFont({
  src: "./fonts/GeistMonoVF.woff",
  variable: "--font-geist-mono",
  weight: "100 900",
});

export const metadata: Metadata = {
  title: "ADHD Focus Companion",
  description: "A mental health toolkit designed with ADHD in mind - hyperfocus management and mood tracking",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
          <ClerkProvider>
            <QueryProvider>
              <PomodoroSessionProvider>
                <TimerStoreBridge />
                <Navbar />
                {children}
                <GlobalPomodoroModal />
              </PomodoroSessionProvider>
            </QueryProvider>
          </ClerkProvider>
        </ThemeProvider>
        <Toaster richColors closeButton />
      </body>
    </html>
  );
}
