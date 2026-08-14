import { API_ENDPOINTS, authenticatedFetch } from "@/lib/api-config";
import type { PomodoroSessionCreationDTO, PomodoroSessionResponseDTO } from "@/types";

export async function savePomodoroSession(
  session: PomodoroSessionCreationDTO,
  getToken: () => Promise<string | null>
): Promise<PomodoroSessionResponseDTO> {
  const response = await authenticatedFetch(API_ENDPOINTS.pomodoro, { method: 'POST', body: JSON.stringify(session) }, getToken);

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to save session (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as PomodoroSessionResponseDTO;
}

export async function getPomodoroSessions(getToken: () => Promise<string | null>): Promise<PomodoroSessionResponseDTO[]> {
  const response = await authenticatedFetch(API_ENDPOINTS.pomodoro, { method: 'GET' }, getToken);

  if (response.status === 204) {
    return [];
  }

  if (!response.ok) {
    throw new Error(`Failed to load pomodoro sessions (${response.status})`);
  }

  return (await response.json()) as PomodoroSessionResponseDTO[];
}

export async function deletePomodoroSession(id: number, getToken: () => Promise<string | null>): Promise<void> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.pomodoro}/${id}`, { method: 'DELETE' }, getToken);

  if (!response.ok) {
    throw new Error(`Failed to delete pomodoro session (${response.status})`);
  }
}
