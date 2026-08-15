'use client'

import { useState } from 'react'
import { QueryClient } from '@tanstack/react-query'
import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client'
import { createAsyncStoragePersister } from '@tanstack/query-async-storage-persister'
import { isNetworkError } from '@/lib/connectivity'

const ONE_DAY_MS = 24 * 60 * 60 * 1000

function makeQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        // Serve cached data instead of erroring when there's no connection.
        networkMode: 'offlineFirst',
        staleTime: 60_000,
        gcTime: ONE_DAY_MS,
      },
      mutations: {
        // Mutations made while offline go "paused" instead of failing outright,
        // and get replayed by resumePausedMutations() once back online.
        networkMode: 'offlineFirst',
        // Mutations default to 0 retries, which skips the retryer's pause/offline check
        // entirely (that logic only runs on a retry attempt) - so a network failure would
        // reject and roll back immediately instead of pausing. Retrying network errors gives
        // it a chance to see we're offline (via connectivity.ts's onlineManager.setOnline)
        // and pause instead. Non-network errors (validation, 404, etc.) still fail fast.
        retry: (failureCount, error) => isNetworkError(error) && failureCount < 5,
      },
    },
  })
}

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(makeQueryClient)

  // `typeof window` only decides which storage backing this persister object holds -
  // it never changes which component/DOM PersistQueryClientProvider renders (children
  // are always rendered unconditionally), so this can't reintroduce a hydration mismatch.
  const [persister] = useState(() =>
    createAsyncStoragePersister({
      storage: typeof window !== 'undefined' ? window.localStorage : undefined,
      key: 'mha-query-cache',
    })
  )

  return (
    <PersistQueryClientProvider
      client={queryClient}
      persistOptions={{
        persister,
        maxAge: ONE_DAY_MS,
        buster: '1',
        dehydrateOptions: {
          // Scope persistence to task queries, but keep the library's default "only
          // persist settled queries" check too - a query still mid-fetch when the cache
          // gets saved carries a live, non-JSON-serializable `promise` reference that
          // breaks restore (`promise.then is not a function`) once deserialize.
          shouldDehydrateQuery: (query) => query.queryKey[0] === 'tasks' && query.state.status === 'success',
        },
      }}
      onSuccess={() => {
        void queryClient.resumePausedMutations()
      }}
    >
      {children}
    </PersistQueryClientProvider>
  )
}
