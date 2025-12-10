import { API_ENDPOINTS, authenticatedFetch } from "@/lib/api-config";
import type { PomodoroSessionCreationDTO } from "@/types";

export async function savePomodoroSession(session: PomodoroSessionCreationDTO, getToken: () => Promise<string | null>) {
  // getToken is a function provided by Clerk's useAuth - pass it through
  return await authenticatedFetch(API_ENDPOINTS.pomodoro, { method: 'POST', body: JSON.stringify(session) }, getToken);
}
