# Data-at-rest encryption plan

Status: **implemented** (pending backfill actually being run against production data - see below). Written 2026-09-04 after a design discussion; captured here so the reasoning survives past the conversation and can inform an ADR.

Implemented so far (branch `feature/data-at-rest-encryption`): the envelope-encryption service, per-user key management (`EncryptionService`, `MasterKeyProvider`, `UserEncryptionKey`/repository, `UserDataKeyService`, `UserDataKeyCache`), and the converters wired into `Task.title/description/category`, `Subtask.title`, `MoodEntry.notes/factors`, `PomodoroSession.notes` (V17/V18 migrations). Both open product decisions below resolved themselves: neither `category` nor `factors` is searched/sorted/filtered at the DB level anywhere in the repository layer, so both were encrypted without needing an app-layer rework.

All ten implementation steps are now done:
- **Ciphertext format versioning**: `EncryptionService` now prefixes every value with `v1:` before the base64 (`v1:base64(IV||ciphertext||tag)`). This isn't secret - it's a format marker so plaintext and ciphertext can be told apart by inspection (`EncryptionService.isEncrypted`) instead of by attempting a decrypt, which is what makes the backfill job below safe to run repeatedly, and gives any future algorithm/format change (`v2:`) a place to branch on.
- **Backfill (`EncryptionBackfillRunner`)**: an `ApplicationRunner`, off by default, gated behind `app.encryption.backfill.enabled` (`APP_ENCRYPTION_BACKFILL_ENABLED` config var). Reads/writes each table directly via `JdbcTemplate`, bypassing Hibernate entirely, so it can select only rows whose column doesn't start with `v1:`, encrypt them under that row's owner's data key, and write them back - already-encrypted rows are left alone, so it's idempotent and safe to run more than once. `mood_entry.factors` gets special handling since a legacy value is JSON array text (from its old JSONB column), not a bare string - it's parsed and re-serialized the same way `EncryptedStringListConverter` does before encrypting. Rows with a null `user_id` (orphaned data) are skipped with a warning rather than failing the whole run. To use it in production: set the config var, deploy, watch the log line reporting rows-updated-per-table, then unset the config var before the next deploy.
- **Heap-dump mitigation**: `-XX:-HeapDumpOnOutOfMemoryError` is now appended (after `$JAVA_OPTS`, so it always wins) in both `Procfile` and `docker-entrypoint.sh`.
- **`byte[]` key hygiene**: `EncryptionService.encrypt(byte[], SecretKey)` now zeroes the caller's plaintext array in a `finally` block after use (covers `UserDataKeyService`'s raw DEK bytes automatically, since they're passed straight into `encrypt`). `MasterKeyProvider` and `UserDataKeyService.unwrap()` zero their raw key byte arrays immediately after wrapping them in a `SecretKeySpec` (which clones internally, so the zeroed copy doesn't affect the key that's actually used). This doesn't - and can't - extend to the `String` overloads used by the converters: Java `String`s are immutable, so a decrypted field value can't be wiped once it exists as one. That residual gap is inherent to the JVM, not something a `byte[]` pattern in `EncryptionService` can close - see the "JVM garbage collection" section below.

## Goal

The app stores personal, sometimes mental-health-adjacent free text (task notes, mood journal entries). The current gap: Postgres on Heroku already encrypts the underlying disk (AES-256 on the storage volumes), but that only defends against physical media theft. **Anyone with valid database credentials — an admin, a leaked connection string, a `pg_dump` backup opened elsewhere — currently reads plaintext.** That's the actual threat to close.

## Two different targets — pick one, know the cost of the other

"Not readable by DB admins, only the user" collapses two distinct goals:

1. **Protect against anyone with DB-level access** (DB admin, stolen backup, leaked DB credential, SQL injection exfil). Achievable now with **application-level field encryption** — the app server still decrypts to serve the data to the user, but the database never holds a key or plaintext.
2. **True zero-knowledge / end-to-end encryption**, where even the app server never sees plaintext — only the browser can decrypt, using a key derived from a secret only the user holds. Clerk owns login/passwords, so this requires a *separate* user-held secret (a "vault passphrase") the app server never receives. It also disables anything server-side that needs to read the content: search, sort-by-title, reminder logic that inspects notes, future AI features.

**Recommendation:** build #1 app-wide now. Treat #2 as a later, opt-in feature for a single highest-sensitivity field (`MoodEntry.notes`) if the residual risk in "What this doesn't protect against" below turns out to matter — not applied app-wide, since it would break search/sort UX for tasks.

## Recommended architecture (tier 1): envelope encryption, per-user key

Per-user keys (rather than one app-wide key) so a single leaked key only exposes one user's data, and so account deletion can crypto-shred a user's data by destroying their key rather than needing a row-by-row wipe.

```
Master Key (KEK)                 lives in Heroku Config Var (APP_MASTER_KEY)
      │                          NEVER stored in Postgres, never baked into the slug
      ▼
wraps → per-user Data Encryption Key (DEK)
      │                          stored encrypted in a new `user_encryption_keys` table
      ▼
encrypts → Task.title, Task.description, Task.category (if free text),
           Subtask.title, MoodEntry.notes, MoodEntry.factors (if free text),
           PomodoroSession.notes  — AES-256-GCM, random IV per value
```

### Implementation steps

1. **`EncryptionService`** — AES-256-GCM, random 12-byte IV per value, stores `base64(IV || ciphertext || tag)`. Loads the KEK from `APP_MASTER_KEY` at startup.
2. **`user_encryption_keys` table** (Flyway `V17`): `user_id` (Clerk `sub`, PK) → `wrapped_dek` (bytea). DEK generated lazily on first write per user, wrapped with the KEK, never stored unwrapped.
3. **DEK caching** — unwrap once and cache briefly (implemented as a small TTL-bounded in-memory cache, not a request-scoped bean as originally planned — request scope broke any repository save happening outside an actual HTTP request, e.g. background jobs, admin tooling, and tests calling repositories directly) to avoid an extra DB round-trip per field access.
4. **JPA `AttributeConverter`s** (`EncryptedStringConverter`, `EncryptedListConverter` for `MoodEntry.factors`) applied via `@Convert`, pulling the current user from `AuthenticationContext.getCurrentUserId()`.
5. **Column widening migration** — ciphertext + IV + tag overhead means fields like `category varchar(50)` need to become `TEXT` before the converters go live.
6. **Backfill migration** — one-off job to encrypt existing rows (generate DEK if missing, encrypt in place, batched/transactional). A short maintenance window is acceptable at this stage rather than a zero-downtime dual-write scheme.
7. **Search/sort trade-off** — encrypted columns can't be used in `LIKE`, `ORDER BY`, or indexes at the DB level. Confirm whether the frontend does DB-level search/sort on any encrypted field; if so, move that to the app layer post-decrypt (fine at personal-planner scale) or add a blind-index column (HMAC-SHA256 of the normalized value) for exact-match lookups only.
8. **Secrets hygiene** — `APP_MASTER_KEY` in Heroku Config Vars, excluded from all logging, backed up somewhere outside the database (password manager, or a real secrets manager later). Losing this key means losing all data permanently.
9. **Testing** — extend the existing Testcontainers integration tests to assert, via raw JDBC bypassing Hibernate, that stored values are ciphertext, not plaintext. Add round-trip encrypt/decrypt unit tests for each converter.
10. **Logging discipline** — audit exception messages and Lombok `@ToString` so decrypted plaintext never leaks into logs.

## What should and shouldn't be encrypted

Heuristic: **encrypt free-text content a human wrote; leave alone anything the system needs to filter, sort, paginate, join, or aggregate.**

### Encrypt

| Field | Why |
|---|---|
| `Task.title` | User-authored, often reveals personal/health context |
| `Task.description` | Same, longer-form |
| `Subtask.title` | Same |
| `MoodEntry.notes` | Journal-like — the most sensitive field in the app |
| `PomodoroSession.notes` | Free text about what was worked on |
| `Task.category` (**if free text**) | Content if user-typed; see "don't encrypt" if it's a picklist |

### Don't encrypt

| Field | Why |
|---|---|
| `id`, `user_id` (Clerk `sub`) | Needed in every `WHERE`/join; encrypting would break ownership lookups |
| `created_at`, `updated_at`, `due_date`, `completed_at`, scheduled date/time | Needed for calendar views, date-range queries, sort, pagination cursors |
| `status`, `priority` | Low cardinality, needed for board/filter views |
| `is_completed` / booleans | Same |
| `position` (drag-drop sort order) | Needed for `ORDER BY` |
| `PomodoroSession`/`TaskTimeEntry` durations, `started_at`, `ended_at`, entry type | Needed for SQL-side `SUM`/`AVG` in stats/charts; encrypting forces app-side aggregation and kills performance |
| `MoodEntry.factors` (**if a fixed picklist**, not free text) | Structured/enumerated data behaves like a tag, not a journal entry; encrypting breaks "show entries where factor = stress" filtering |
| Foreign keys | Needed for joins |

**Open product decisions this depends on:** is `category` free text or a picklist? Is `factors` free text or a picklist? These decide which side of the line those two fields fall on.

## Comparison: this approach vs alternatives vs no encryption

| Approach | Advantages | Disadvantages |
|---|---|---|
| **No encryption (status quo)** | Simplest; full SQL search/sort/filter/aggregation; zero engineering cost | Any DB admin, leaked credential, or stolen backup reads everything in plaintext; doesn't meet the stated goal at all |
| **Disk-level encryption only** (Heroku default) | Free, already on, protects against physical disk/media theft | Doesn't stop anyone with valid DB credentials — the actual threat named here |
| **pgcrypto** (`pgp_sym_encrypt`/`decrypt`, key passed per query) | No ORM converter layer; encryption logic centralized in SQL | The key transits through the DB engine on every query — an admin who can see query logs/`pg_stat_statements` or modify a function/trigger can potentially capture the key or plaintext. **Weakest option against the DB-admin threat model specifically**, since the admin controls the process doing the decrypting. No per-user isolation or crypto-shred |
| **Application-level field encryption (this plan)** | Key material never touches Postgres in any form — DB admin cannot decrypt via DB access, backups, or query logs; per-user keys limit blast radius and enable crypto-shred deletion; fits the existing JPA/Flyway/Testcontainers stack; AES-GCM overhead is negligible | Real engineering lift (converters, migration, key management, request-scoped caching); breaks DB-side search/sort/filter on encrypted fields; ciphertext columns grow (~1.4x + overhead); losing the KEK is permanent data loss; app server still sees plaintext at runtime — a compromised server (not DB) is residual risk |
| **Heroku Shield / Private Spaces (enterprise)** | Network isolation, HIPAA-eligible compliance posture | Enterprise pricing; solves a network/compliance problem, not the credentialed-admin-reads-plaintext problem — doesn't meet the stated goal on its own |
| **True end-to-end / client-side encryption** | The only option where nobody but the user can ever decrypt — including a compromised app server; the strongest possible answer to "only the user" | Large lift: client-side crypto (WebCrypto/Argon2), a separate passphrase UX with no recoverable "forgot password" path; kills server-side search/sort/aggregation and any feature needing to read content server-side; multi-device sync needs its own key-distribution scheme |

**Net read:** app-level field encryption with per-user envelope keys fits what's actually needed here, reuses the existing stack, and doesn't sacrifice search/sort/stats on the metadata that needs them. pgcrypto looks similar on paper but is meaningfully weaker against this specific threat model. True E2EE is the strongest possible guarantee but a separate, much larger project — worth keeping as a future opt-in for `MoodEntry.notes` alone.

## Residual risks (things this plan does *not* solve)

### JVM garbage collection / in-memory plaintext

Threat model note: this only matters to someone with memory-level access to the running dyno (heap dump, crash dump, debugger attach) — a higher bar than "has DB credentials," which is what the plan above defends against.

- Java `String`s are immutable — every decryption produces copies that linger on the heap until GC reclaims them, and reclaiming isn't zeroing; the bytes are just marked free until overwritten.
- G1's copying/compacting GC physically relocates live objects; there's no single point to "wipe."
- **The largest practical exposure is heap dumps, not diffuse GC crumbs.** `-XX:+HeapDumpOnOutOfMemoryError`, or a manual/APM-triggered heap snapshot, writes a full memory image to disk — plaintext *and* the unwrapped KEK, in the clear, in one file.

Mitigations (best-effort — the JVM has no guaranteed secure-memory primitive):
- Hold key material as `byte[]`, not `String`; `Arrays.fill(key, (byte)0)` in a `finally` block after use.
- Decrypt as late as possible (serialization boundary, not eager entity load); don't hold references longer than needed.
- Never cache decrypted values in Redis/Ehcache/Caffeine — cache ciphertext, decrypt per request.
- Exclude sensitive fields from Lombok `@ToString`/logging.
- Disable `HeapDumpOnOutOfMemoryError` in production; if a dump is ever taken for debugging, treat the file as a secret and delete it immediately.

This is inherent to any JVM process that decrypts server-side, not specific to this design. True E2EE doesn't fix it either — it just moves the same category of problem to the browser's JS heap; its actual mitigation is removing the app server from the set of parties holding plaintext/keys at all, which is the larger architectural jump, not a small addition to this plan.

### Heroku dynos being ephemeral

Mostly favorable: no persistent local disk means nothing accumulates across restarts (deploys, ~24h mandatory cycling, crashes, scaling events) — anything in heap disappears completely and non-recoverably when the dyno dies. An OOM kill (R14) typically destroys the dyno's ephemeral disk before a heap dump could be written anywhere durable, unless something has been explicitly wired to export one.

Two real implications:
1. **The KEK must always come from Config Vars, never a file baked into the slug/image** — already the design, ephemerality just makes it non-negotiable.
2. **Heroku app-level access becomes part of the trust boundary.** Anyone with collaborator/admin access to the Heroku app can run `heroku config` and read `APP_MASTER_KEY` directly, no database access needed. Honest statement of what this plan achieves: *DB admins / backup thieves / leaked DB credentials can't read the data — true. Heroku app admins can't read the data — not true*, unless Heroku app access is separately restricted. Closing that gap later means a real secrets manager (AWS Secrets Manager, GCP KMS, Vault) with audited, API-based key access instead of a raw env var held in every dyno's memory.

Minor operational note: rotating the KEK means changing a Config Var, which restarts every dyno at once — acceptable at this scale, just not invisible.

## Summary of what this plan guarantees vs doesn't

- ✅ A Heroku Postgres admin, a stolen backup, or a leaked read-only DB credential sees only ciphertext.
- ✅ Per-user blast-radius containment; clean crypto-shred on account deletion.
- ❌ Does not protect against compromise of the app server itself (it holds the KEK and decrypts at runtime) — that's the tier-2/E2EE problem.
- ❌ Does not protect against someone with Heroku app admin access (Config Vars are readable there) — a separate access-control question, not a crypto one.
