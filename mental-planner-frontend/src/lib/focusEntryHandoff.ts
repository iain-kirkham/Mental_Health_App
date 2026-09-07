/**
 * Bridges the gap between a Focus session's time entry being created (immediately, at run
 * completion, by TimerStoreBridge) and its reflection being attached (later, whenever the user
 * submits the reflection form) - the two happen at different times, driven by different
 * components, with no guarantee the create-POST has resolved before the reflection is ready.
 *
 * Keyed by the run's runKey (see timerStore) rather than always overwriting a single slot, so a
 * fast second run's reflection can't be attached to the first run's entry. Deliberately a plain
 * module-level variable, not store state: the in-flight Promise isn't serializable and has no
 * business surviving a reload.
 */
let pending: { runKey: string; entryId: Promise<number | null> } | null = null;

/** Registers the in-flight (or already-settled) id for a run's just-created time entry. The
 * promise must never reject - callers should catch internally and resolve null on failure, so a
 * slow-to-reflect user never produces an unhandled rejection. */
export function beginFocusEntry(runKey: string, entryId: Promise<number | null>): void {
  pending = { runKey, entryId };
}

/** Resolves to the entry id for the given run, or null if that run is unknown (already claimed,
 * never registered, or the create-POST failed). */
export async function claimFocusEntry(runKey: string): Promise<number | null> {
  if (pending === null || pending.runKey !== runKey) return null;
  return pending.entryId;
}

/** Test-only reset hook. */
export function clearFocusEntry(): void {
  pending = null;
}
