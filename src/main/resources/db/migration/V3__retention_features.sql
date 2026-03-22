-- Subscription table (FK to users)
CREATE TABLE subscriptions (
    id               BIGSERIAL PRIMARY KEY,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP,
    user_id          BIGINT NOT NULL UNIQUE REFERENCES users(id),
    status           VARCHAR(20) NOT NULL DEFAULT 'FREE',  -- FREE | PREMIUM | LAPSED
    stripe_customer_id VARCHAR(255),
    stripe_sub_id    VARCHAR(255),
    period_end       TIMESTAMP
);

-- Add time tracking columns to progress
ALTER TABLE progress ADD COLUMN IF NOT EXISTS lesson_started_at TIMESTAMP;
ALTER TABLE progress ADD COLUMN IF NOT EXISTS time_spent_seconds INTEGER;

-- Index for paywall query (count today's completed lessons per child)
CREATE INDEX IF NOT EXISTS idx_progress_child_completed_at
    ON progress(child_id, completed_at)
    WHERE completed = true;

-- Index for parent dashboard queries
CREATE INDEX IF NOT EXISTS idx_progress_child_lesson
    ON progress(child_id);
