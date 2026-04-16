import { API_ENDPOINTS, authenticatedFetch } from '@/lib/api-config'
import type { JobApplicationRequestDTO, JobApplicationResponseDTO } from '@/types'

export async function getJobApplications(getToken: () => Promise<string | null>): Promise<JobApplicationResponseDTO[]> {
  const response = await authenticatedFetch(API_ENDPOINTS.jobSearch, { method: 'GET' }, getToken)

  if (response.status === 204) {
    return []
  }

  if (!response.ok) {
    throw new Error(`Failed to load job applications (${response.status})`)
  }

  return (await response.json()) as JobApplicationResponseDTO[]
}

export async function createJobApplication(
  payload: JobApplicationRequestDTO,
  getToken: () => Promise<string | null>
): Promise<JobApplicationResponseDTO> {
  const response = await authenticatedFetch(
    API_ENDPOINTS.jobSearch,
    { method: 'POST', body: JSON.stringify(payload) },
    getToken
  )

  if (!response.ok) {
    throw new Error(`Failed to create job application (${response.status})`)
  }

  return (await response.json()) as JobApplicationResponseDTO
}

export async function updateJobApplication(
  id: number,
  payload: JobApplicationRequestDTO,
  getToken: () => Promise<string | null>
): Promise<JobApplicationResponseDTO> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.jobSearch}/${id}`,
    { method: 'PUT', body: JSON.stringify(payload) },
    getToken
  )

  if (!response.ok) {
    throw new Error(`Failed to update job application (${response.status})`)
  }

  return (await response.json()) as JobApplicationResponseDTO
}

export async function deleteJobApplication(id: number, getToken: () => Promise<string | null>): Promise<void> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.jobSearch}/${id}`, { method: 'DELETE' }, getToken)

  if (!response.ok) {
    throw new Error(`Failed to delete job application (${response.status})`)
  }
}


