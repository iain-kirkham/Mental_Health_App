import { describe, expect, it, vi, beforeEach } from 'vitest'
import { API_ENDPOINTS, authenticatedFetch } from '@/lib/api-config'
import { createJobApplication, deleteJobApplication, getJobApplications, updateJobApplication } from '@/lib/job-search-api'

vi.mock('@/lib/api-config', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api-config')>('@/lib/api-config')
  return {
    ...actual,
    authenticatedFetch: vi.fn(),
  }
})

describe('job-search-api', () => {
  const mockedAuthenticatedFetch = vi.mocked(authenticatedFetch)
  const getToken = vi.fn(async () => 'token')

  beforeEach(() => {
    mockedAuthenticatedFetch.mockReset()
  })

  it('creates a job application using the job search endpoint', async () => {
    const payload = { companyName: 'Acme', roleTitle: 'Frontend Engineer', status: 'APPLIED' as const }
    const responseBody = { id: 10, ...payload }

    mockedAuthenticatedFetch.mockResolvedValue(
      new Response(JSON.stringify(responseBody), { status: 201, headers: { 'Content-Type': 'application/json' } })
    )

    const response = await createJobApplication(payload, getToken)

    expect(response).toEqual(responseBody)
    expect(mockedAuthenticatedFetch).toHaveBeenCalledWith(
      API_ENDPOINTS.jobSearch,
      { method: 'POST', body: JSON.stringify(payload) },
      getToken
    )
  })

  it('returns an empty list when the backend responds with 204', async () => {
    mockedAuthenticatedFetch.mockResolvedValue(new Response(null, { status: 204 }))

    const response = await getJobApplications(getToken)

    expect(response).toEqual([])
    expect(mockedAuthenticatedFetch).toHaveBeenCalledWith(API_ENDPOINTS.jobSearch, { method: 'GET' }, getToken)
  })

  it('deletes a job application by id', async () => {
    mockedAuthenticatedFetch.mockResolvedValue(new Response(null, { status: 204 }))

    await deleteJobApplication(12, getToken)

    expect(mockedAuthenticatedFetch).toHaveBeenCalledWith(
      `${API_ENDPOINTS.jobSearch}/12`,
      { method: 'DELETE' },
      getToken
    )
  })

  it('updates a job application status by id', async () => {
    const payload = { companyName: 'Acme', roleTitle: 'Frontend Engineer', status: 'INTERVIEWING' as const }
    const responseBody = { id: 12, ...payload }

    mockedAuthenticatedFetch.mockResolvedValue(
      new Response(JSON.stringify(responseBody), { status: 200, headers: { 'Content-Type': 'application/json' } })
    )

    const response = await updateJobApplication(12, payload, getToken)

    expect(response).toEqual(responseBody)
    expect(mockedAuthenticatedFetch).toHaveBeenCalledWith(
      `${API_ENDPOINTS.jobSearch}/12`,
      { method: 'PUT', body: JSON.stringify(payload) },
      getToken
    )
  })
})


