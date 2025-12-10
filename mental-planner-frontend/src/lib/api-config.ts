// API Configuration
// Centralized configuration for backend API calls

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  pomodoro: `${API_BASE_URL}/api/pomodoro`,
  mood: `${API_BASE_URL}/api/mood`,
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

  return fetch(url, {
    ...options,
    headers,
  });
}

export default API_BASE_URL;

