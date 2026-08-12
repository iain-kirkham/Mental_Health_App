import { API_ENDPOINTS, authenticatedFetch } from '@/lib/api-config'
import type { MoodEntryResponseDTO } from '@/types'

export async function getMoodEntries(getToken: () => Promise<string | null>): Promise<MoodEntryResponseDTO[]> {
  const response = await authenticatedFetch(API_ENDPOINTS.mood, { method: 'GET' }, getToken)

  if (response.status === 204) {
    return []
  }

  if (!response.ok) {
    throw new Error(`Failed to load mood entries (${response.status})`)
  }

  return (await response.json()) as MoodEntryResponseDTO[]
}

export async function deleteMoodEntry(id: number, getToken: () => Promise<string | null>): Promise<void> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.mood}/${id}`, { method: 'DELETE' }, getToken)

  if (!response.ok) {
    throw new Error(`Failed to delete mood entry (${response.status})`)
  }
}
