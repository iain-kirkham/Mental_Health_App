import { onlineManager } from '@tanstack/react-query'

const PROBE_INTERVAL_MS = 5000

let probeTimer: ReturnType<typeof setInterval> | null = null

/** Raw fetch()-level failures (connection refused, DNS failure, offline) throw a browser
 * TypeError with wording like "Failed to fetch" or "NetworkError when attempting to fetch
 * resource." - distinct from the friendly `new Error(message)` thrown for HTTP-level errors
 * elsewhere in the API layer. */
export function isNetworkError(error: unknown): boolean {
  return error instanceof TypeError && /fetch|network/i.test(error.message)
}

function stopProbing() {
  if (probeTimer) {
    clearInterval(probeTimer)
    probeTimer = null
  }
}

if (typeof window !== 'undefined') {
  onlineManager.subscribe((online) => {
    if (online) stopProbing()
  })
}

function startProbing(baseUrl: string) {
  if (probeTimer) return
  probeTimer = setInterval(() => {
    // `no-cors` avoids a cross-origin failure masking the real answer - we only care whether
    // the TCP/HTTP round trip succeeds at all, not what the response contains.
    fetch(baseUrl, { method: 'HEAD', mode: 'no-cors' })
      .then(() => onlineManager.setOnline(true))
      .catch(() => {})
  }, PROBE_INTERVAL_MS)
}

/** React Query's built-in `onlineManager` only tracks the browser's network interface
 * (`navigator.onLine` + online/offline events), which stays "online" even when the API server
 * itself is unreachable (e.g. backend not running). Call this after every fetch attempt so
 * connectivity tracking reflects real API reachability - marking offline lets in-flight
 * mutation retries pause and queue (via `networkMode: 'offlineFirst'`) instead of erroring out
 * and rolling back, and starts polling so we flip back online automatically once the backend
 * comes back, which also triggers React Query's automatic `resumePausedMutations()`. */
export function reportFetchOutcome(baseUrl: string, error: unknown) {
  if (!isNetworkError(error)) return
  onlineManager.setOnline(false)
  startProbing(baseUrl)
}

export function reportFetchSuccess() {
  onlineManager.setOnline(true)
}

/** Rewords raw browser fetch() failures into something a user can act on; anything else
 * thrown by the API layer is already a friendly `new Error(message)`, so it's shown as-is. */
export function toFriendlyMessage(error: unknown, fallbackMessage: string): string {
  if (isNetworkError(error)) return "Couldn't reach the server. Check your connection and try again."
  return error instanceof Error ? error.message : fallbackMessage
}
