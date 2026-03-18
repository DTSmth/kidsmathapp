# TODOS

## P3 — Design Debt

### DESIGN.md Maintenance
Keep `frontend/DESIGN.md` up to date when new components are added or design decisions change.

**Why:** A stale DESIGN.md is worse than none — it creates confusion when following outdated specs.
**Effort:** S (ongoing — ~5 min per new component)
**Priority:** P3
**How:** Add a checklist item to any PR that introduces new UI components: "Did you update DESIGN.md?"

---

### Sound Effects
Add audio feedback for correct/wrong answers using the Web Audio API (no audio files — generate simple tones programmatically). Add a mute toggle in the header or settings.

**Why:** For kids ages 5-8, audio cues reinforce the reward loop. A cheerful "ding" on correct and a soft "whomp" on wrong are proven engagement signals in ed-tech research.
**Effort:** S (human: ~2h / CC: ~15 min)
**Priority:** P3
**Depends on:** MVP completion (quiz flow must exist first)
**Where to start:** Create `src/hooks/useQuizSounds.ts` using `AudioContext`, expose `playCorrect()` and `playWrong()` functions. Add `<SoundToggle />` component to Header.

---

### CI/CD Pipeline
Add a GitHub Actions workflow that runs `mvn test` and `npm run lint` on every push to master.

**Why:** Without CI, test regressions only get caught when you remember to run tests locally. As the test suite grows (25+ cases added in this plan), this becomes critical.
**Effort:** S (human: ~2h / CC: ~15 min)
**Priority:** P2
**Where to start:** `.github/workflows/ci.yml` — two jobs: backend (`mvn test`) and frontend (`npm run lint && npm run build`). Add Postgres service container for backend tests.

---

### Flyway Production Baseline
Before any real production deployment, add a Flyway V0 baseline migration (capturing the current schema from `ddl-auto` output) and a CI step that validates all migrations apply cleanly.

**Why:** Flyway V1 (unique constraints) is added in the MVP. Without a V0 baseline, future team members can't bring up a clean DB from migrations alone — they need to rely on `ddl-auto=create`, which is dangerous in prod.
**Effort:** S (human: ~3h / CC: ~15 min)
**Priority:** P2
**Depends on:** CI/CD pipeline (run migration check in CI).
**Where to start:** Run `mvn flyway:baseline` against the local DB to capture the current schema, then commit the resulting `V0__baseline.sql`.

---

## P2 — Phase 2 (After MVP Ships)

### JWT Token Refresh Endpoint
Add `POST /api/v1/auth/refresh` that accepts a valid JWT and returns a new one with a fresh 24h expiry.

**Why:** The current pre-flight expiry check (added in MVP) warns parents when their session is < 30 min from expiry, but doesn't silently refresh. A child mid-lesson on day 2 would still hit this edge case.
**Effort:** M (human: ~4h / CC: ~20 min)
**Priority:** P2
**Depends on:** Nothing — standalone backend feature
**Where to start:** `AuthService`, add `refreshToken(String token)` method; new `RefreshResponse` DTO; update `SecurityConfig` to permit the new endpoint without auth.

---

### Answer Rate Limiting
Add rate limiting to `POST /api/v1/questions/{id}/check` — 60 checks/minute per authenticated user.

**Why:** Currently anyone with a valid JWT can call this endpoint in a loop to discover correct answers programmatically. Low risk for a kids app but required before any public launch.
**Effort:** S (human: ~2h / CC: ~10 min)
**Priority:** P3
**Depends on:** Nothing — add Bucket4j dependency, configure in `SecurityConfig` or as a filter.

---

### Games Section
Build `GameController`, `GameService`, and the frontend games browser + game flow using the already-seeded `Game` and `GameScore` entities.

**Why:** The data model is complete and DataSeeder already seeds game definitions. Math games (speed drills, number matching, pattern recognition) are a proven engagement driver for kids ages 5-10. This is Phase 2's biggest retention feature.
**Effort:** L (human: ~3 days / CC: ~1.5h)
**Priority:** P2
**Depends on:** MVP completion (lesson flow) — establishes component patterns that games can reuse.
**Where to start:** `GameRepository`, new `GameService`, `GameController` at `/api/v1/games`, then frontend `Games.tsx` page.

---

### Adaptive Difficulty Engine
Track per-child accuracy per topic (rolling 10-question window) and automatically serve questions from the appropriate difficulty bucket (EASY/MEDIUM/HARD enum already on `Question` entity).

**Why:** This is the product differentiator that transforms KidsMathApp from a "content player" into a "learning engine." A child who aces 10 easy addition questions should get medium ones. A child struggling with hard subtraction should get easy ones. The `Difficulty` enum on `Question` is already in place.
**Effort:** XL (human: ~2 weeks / CC: ~2h)
**Priority:** P2
**Depends on:** Games section (can share the accuracy tracking infrastructure).
**Where to start:** New `AccuracyTracker` service that maintains a rolling per-child-per-topic accuracy score; modify `QuestionService` to query by difficulty bucket matching current accuracy.
