# Actual minutes' timer checkpoint stays client-trusted, not delta-based

Giving the Task entity's Actual minutes invariant a proper seam (`applyManualEntry`/`unwindManualEntry`/`recordTimerCheckpoint`) raised the question of whether `recordTimerCheckpoint` should keep trusting an absolute, client-computed total for stopwatch/countdown runs, or switch to a server-verified delta so it can't collide with a concurrent manual entry. We chose to keep it absolute and client-trusted: the delta contract would require a coordinated frontend change (`TimerStoreBridge.tsx` currently sends an absolute total, not a delta), and shipping the backend half alone would silently corrupt every stopwatch/countdown persist by misinterpreting an absolute total as a delta. The real fix — a server-tracked timer session that doesn't need to trust the client at all — is scoped into the separate Sunsama-style time-tracking rework already planned, not this seam fix.

## Consequences

A stopwatch/countdown persist can still race with a concurrent manual entry and silently overwrite it, since `recordTimerCheckpoint` sets the total rather than reconciling against it. Don't "fix" this in isolation by switching the backend to a delta contract without updating the frontend in the same change.
