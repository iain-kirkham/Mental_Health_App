// API Configuration
// Centralized configuration for backend API calls

import { reportFetchOutcome, reportFetchSuccess } from '@/lib/connectivity';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  timeEntries: `${API_BASE_URL}/api/time-entries`,
  mood: `${API_BASE_URL}/api/mood`,
  tasks: `${API_BASE_URL}/api/tasks`,
} as const;

/**
 * Makes an authenticated API request with the Clerk JWT token.
 * This function should be used for all API calls to the backend that require authentication.
 * 
 * @param url - The API endpoint URL
 * @param options - Fetch options (method, body, etc.)
 * @param getToken - Clerk's getToken function from useAuth()
 * @returns Promise<Response>
 */
export async function authenticatedFetch(
  url: string, 
  options: RequestInit = {}, 
  getToken: () => Promise<string | null>
): Promise<Response> {
  const token = await getToken();
  
  if (!token) {
    throw new Error('No authentication token available');
  }

  const headers = {
    ...options.headers,
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  try {
    const response = await fetch(url, {
      ...options,
      headers,
    });
    reportFetchSuccess();
    return response;
  } catch (error) {
    reportFetchOutcome(API_BASE_URL, error);
    throw error;
  }
}

export default API_BASE_URL;

