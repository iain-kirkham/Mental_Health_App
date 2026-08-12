'use client'

import React, { FormEvent, useEffect, useState } from 'react'
import { useAuth } from '@clerk/nextjs'
import PageHeader from '@/components/PageHeader'
import PageInset from '@/components/PageInset'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  createJobApplication,
  deleteJobApplication,
  getJobApplications,
  updateJobApplication,
} from '@/lib/job-search-api'
import type { JobApplicationResponseDTO, JobApplicationStatus } from '@/types'

type JobStatusOption = {
  label: string
  value: JobApplicationStatus
}

const STATUS_OPTIONS: JobStatusOption[] = [
  { label: 'Applied', value: 'APPLIED' },
  { label: 'Interviewing', value: 'INTERVIEWING' },
  { label: 'Offer', value: 'OFFER' },
  { label: 'Rejected', value: 'REJECTED' },
]

function isJobStatus(value: unknown): value is JobApplicationStatus {
  return STATUS_OPTIONS.some((option) => option.value === value)
}

function getStatusLabel(status: JobApplicationStatus): string {
  return STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status
}

function getStatusPillClass(status: JobApplicationStatus): string {
  switch (status) {
    case 'INTERVIEWING':
      return 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200'
    case 'OFFER':
      return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200'
    case 'REJECTED':
      return 'bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-200'
    default:
      return 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-200'
  }
}

export default function JobSearchTracker() {
  const { getToken } = useAuth()
  const [entries, setEntries] = useState<JobApplicationResponseDTO[]>([])
  const [companyName, setCompanyName] = useState('')
  const [roleTitle, setRoleTitle] = useState('')
  const [status, setStatus] = useState<JobApplicationStatus>('APPLIED')
  const [formError, setFormError] = useState('')
  const [submitStatus, setSubmitStatus] = useState<'idle' | 'success' | 'error'>('idle')
  const [errorMessage, setErrorMessage] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<number | null>(null)
  const [isUpdatingId, setIsUpdatingId] = useState<number | null>(null)

  useEffect(() => {
    const loadEntries = async () => {
      try {
        const loadedEntries = await getJobApplications(getToken)
        setEntries(loadedEntries)
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Unable to load job applications.'
        setSubmitStatus('error')
        setErrorMessage(message)
      } finally {
        setIsLoading(false)
      }
    }

    void loadEntries()
  }, [])

  const handleAddEntry = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const trimmedCompanyName = companyName.trim()
    const trimmedRoleTitle = roleTitle.trim()

    if (!trimmedCompanyName || !trimmedRoleTitle) {
      setFormError('Please enter both a company and role title.')
      return
    }

    setIsSubmitting(true)
    setFormError('')
    setSubmitStatus('idle')
    setErrorMessage('')

    try {
      const createdEntry = await createJobApplication(
        {
          companyName: trimmedCompanyName,
          roleTitle: trimmedRoleTitle,
          status,
        },
        getToken
      )

      setEntries((currentEntries) => [createdEntry, ...currentEntries])
      setCompanyName('')
      setRoleTitle('')
      setStatus('APPLIED')
      setSubmitStatus('success')
      setTimeout(() => setSubmitStatus('idle'), 3000)
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to save job application.'
      setSubmitStatus('error')
      setErrorMessage(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDeleteEntry = async (id: number) => {
    setIsDeletingId(id)
    setSubmitStatus('idle')
    setErrorMessage('')
    try {
      await deleteJobApplication(id, getToken)
      setEntries((currentEntries) => currentEntries.filter((entry) => entry.id !== id))
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete job application.'
      setSubmitStatus('error')
      setErrorMessage(message)
    } finally {
      setIsDeletingId(null)
    }
  }

  const handleUpdateEntryStatus = async (id: number, nextStatus: JobApplicationStatus) => {
    const existingEntry = entries.find((entry) => entry.id === id)
    if (!existingEntry || existingEntry.status === nextStatus) {
      return
    }

    setIsUpdatingId(id)
    setSubmitStatus('idle')
    setErrorMessage('')

    try {
      const updatedEntry = await updateJobApplication(
        id,
        {
          companyName: existingEntry.companyName,
          roleTitle: existingEntry.roleTitle,
          status: nextStatus,
        },
        getToken
      )

      setEntries((currentEntries) =>
        currentEntries.map((entry) => (entry.id === id ? updatedEntry : entry))
      )
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to update job status.'
      setSubmitStatus('error')
      setErrorMessage(message)
    } finally {
      setIsUpdatingId(null)
    }
  }

  return (
    <>
      <PageHeader
        title={<>Job Search Tracker</>}
        subtitle={<>Keep a quick list of applications and where each one stands.</>}
        size="wide"
      />
      <div className="pt-4 md:pt-6" />

      <PageInset size="wide" className="pb-8 md:pb-10 space-y-6">
        {submitStatus === 'success' ? (
          <p className="rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">
            Application saved.
          </p>
        ) : null}

        {submitStatus === 'error' ? (
          <p className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-800">
            {errorMessage || 'Something went wrong. Please try again.'}
          </p>
        ) : null}

        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Add application</CardTitle>
            <CardDescription>Track company, role title, and current status.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleAddEntry} className="grid grid-cols-1 gap-3 md:grid-cols-4 md:items-end">
              <div className="space-y-1 md:col-span-2">
                <label htmlFor="companyName" className="text-sm font-medium text-slate-700 dark:text-slate-200">
                  Company
                </label>
                <Input
                  id="companyName"
                  value={companyName}
                  onChange={(event) => setCompanyName(event.target.value)}
                  placeholder="e.g. Acme Ltd"
                />
              </div>

              <div className="space-y-1">
                <label htmlFor="roleTitle" className="text-sm font-medium text-slate-700 dark:text-slate-200">
                  Role title
                </label>
                <Input
                  id="roleTitle"
                  value={roleTitle}
                  onChange={(event) => setRoleTitle(event.target.value)}
                  placeholder="Frontend Developer"
                />
              </div>

              <div className="space-y-1">
                <label className="text-sm font-medium text-slate-700 dark:text-slate-200">Status</label>
                <Select
                  value={status}
                  onValueChange={(value) => {
                    if (isJobStatus(value)) {
                      setStatus(value)
                    }
                  }}
                >
                  <SelectTrigger aria-label="Application status">
                    <SelectValue placeholder="Select status">
                      {getStatusLabel(status)}
                    </SelectValue>
                  </SelectTrigger>
                  <SelectContent>
                    {STATUS_OPTIONS.map((statusOption) => (
                      <SelectItem key={statusOption.value} value={statusOption.value}>
                        {statusOption.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="md:col-span-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <Button type="submit" className="w-full sm:w-auto" disabled={isSubmitting}>
                  {isSubmitting ? 'Saving...' : 'Add entry'}
                </Button>
                {formError ? <p className="text-sm text-rose-600 dark:text-rose-300">{formError}</p> : null}
              </div>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Your applications</CardTitle>
            <CardDescription>{entries.length} tracked role{entries.length === 1 ? '' : 's'}</CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <p className="text-sm text-muted-foreground">Loading applications...</p>
            ) : entries.length === 0 ? (
              <p className="text-sm text-muted-foreground">No applications yet. Add your first one above.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[520px] border-separate border-spacing-0">
                  <thead>
                    <tr>
                      <th className="border-b border-slate-200 dark:border-slate-700 px-3 py-2 text-left text-sm font-medium text-slate-600 dark:text-slate-300">
                        Company
                      </th>
                      <th className="border-b border-slate-200 dark:border-slate-700 px-3 py-2 text-left text-sm font-medium text-slate-600 dark:text-slate-300">
                        Role title
                      </th>
                      <th className="border-b border-slate-200 dark:border-slate-700 px-3 py-2 text-left text-sm font-medium text-slate-600 dark:text-slate-300">
                        Status
                      </th>
                      <th className="border-b border-slate-200 dark:border-slate-700 px-3 py-2 text-right text-sm font-medium text-slate-600 dark:text-slate-300">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {entries.map((entry) => (
                      <tr key={entry.id}>
                        <td className="border-b border-slate-100 dark:border-slate-800 px-3 py-3 text-sm text-slate-900 dark:text-slate-100">
                          {entry.companyName}
                        </td>
                        <td className="border-b border-slate-100 dark:border-slate-800 px-3 py-3 text-sm text-slate-900 dark:text-slate-100">
                          {entry.roleTitle}
                        </td>
                        <td className="border-b border-slate-100 dark:border-slate-800 px-3 py-3 text-sm min-w-[180px]">
                          <Select
                            value={entry.status}
                            onValueChange={(value) => {
                              if (isJobStatus(value)) {
                                void handleUpdateEntryStatus(entry.id, value)
                              }
                            }}
                            disabled={isUpdatingId === entry.id || isDeletingId === entry.id}
                          >
                            <SelectTrigger
                              className={getStatusPillClass(entry.status)}
                              aria-label={`Update status for ${entry.companyName}`}
                            >
                              <SelectValue>{getStatusLabel(entry.status)}</SelectValue>
                            </SelectTrigger>
                            <SelectContent>
                              {STATUS_OPTIONS.map((statusOption) => (
                                <SelectItem key={`${entry.id}-${statusOption.value}`} value={statusOption.value}>
                                  {statusOption.label}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </td>
                        <td className="border-b border-slate-100 dark:border-slate-800 px-3 py-3 text-right">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleDeleteEntry(entry.id)}
                            disabled={isDeletingId === entry.id}
                          >
                            {isDeletingId === entry.id ? 'Deleting...' : 'Delete'}
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>
      </PageInset>
    </>
  )
}


