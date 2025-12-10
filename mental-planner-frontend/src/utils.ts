import { format } from 'date-fns';

// Utility functions for ADHD Focus Companion

/**
 * Format a Date object to ISO date string (yyyy-MM-dd)
 */
export const getDateString = (date: Date): string => format(date, 'yyyy-MM-dd');

/**
 * Format a Date object to a readable format
 */
export const formatReadableDate = (date: Date): string => format(date, 'MMM dd, yyyy');

/**
 * Format a Date object to include time
 */
export const formatDateTime = (date: Date): string => format(date, 'MMM dd, yyyy - HH:mm');
