-- V2: Engagement Engine

-- New table: avatar_item
CREATE TABLE avatar_item (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    name VARCHAR(255) NOT NULL,
    emoji VARCHAR(50),
    tier VARCHAR(50) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    unlock_condition TEXT
);

-- New table: child_inventory
CREATE TABLE child_inventory (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    child_id BIGINT NOT NULL REFERENCES children(id),
    item_id BIGINT NOT NULL REFERENCES avatar_item(id),
    equipped_slot VARCHAR(50),
    earned_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_child_item UNIQUE (child_id, item_id)
);

-- Alter game_scores: add game_mode
ALTER TABLE game_scores ADD COLUMN game_mode VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

-- Alter streaks: add daily_bonus_claimed
ALTER TABLE streaks ADD COLUMN daily_bonus_claimed BOOLEAN NOT NULL DEFAULT FALSE;

-- Indexes
CREATE INDEX idx_child_parent_stars ON children(parent_id, total_stars DESC);
CREATE INDEX idx_child_parent_streak ON children(parent_id, current_streak DESC);
CREATE INDEX idx_game_score_leaderboard ON game_scores(game_id, game_mode, score DESC);
CREATE INDEX idx_streak_child_date ON streaks(child_id, date DESC);
