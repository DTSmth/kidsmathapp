-- V1: Full schema baseline
-- Tables are created in dependency order (referenced tables first).

-- ── Independent tables ────────────────────────────────────────────────────────

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE topics (
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    icon_name   VARCHAR(255),
    order_index INTEGER,
    grade_level VARCHAR(50)
);

CREATE TABLE achievements (
    id               BIGSERIAL PRIMARY KEY,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP,
    name             VARCHAR(255) NOT NULL,
    description      VARCHAR(255),
    badge_image_url  VARCHAR(255),
    unlock_condition TEXT,
    stars_bonus      INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE games (
    id                BIGSERIAL PRIMARY KEY,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP,
    name              VARCHAR(255) NOT NULL,
    description       VARCHAR(255),
    game_type         VARCHAR(50) NOT NULL,
    icon_name         VARCHAR(255),
    base_stars_reward INTEGER,
    time_limit        INTEGER
);

-- ── Tables that depend on users ───────────────────────────────────────────────

CREATE TABLE children (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP,
    name            VARCHAR(255) NOT NULL,
    avatar_id       VARCHAR(255),
    birth_date      DATE,
    grade_level     VARCHAR(50),
    total_stars     INTEGER NOT NULL DEFAULT 0,
    current_streak  INTEGER NOT NULL DEFAULT 0,
    parent_id       BIGINT  NOT NULL REFERENCES users(id)
);

-- ── Tables that depend on topics ──────────────────────────────────────────────

CREATE TABLE lessons (
    id           BIGSERIAL PRIMARY KEY,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP,
    title        VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    content      TEXT,
    order_index  INTEGER,
    stars_reward INTEGER NOT NULL DEFAULT 10,
    topic_id     BIGINT  NOT NULL REFERENCES topics(id)
);

-- ── Join table: games ↔ topics ────────────────────────────────────────────────

CREATE TABLE game_topics (
    game_id  BIGINT NOT NULL REFERENCES games(id),
    topic_id BIGINT NOT NULL REFERENCES topics(id),
    PRIMARY KEY (game_id, topic_id)
);

-- ── Tables that depend on lessons / games ─────────────────────────────────────

CREATE TABLE questions (
    id             BIGSERIAL PRIMARY KEY,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP,
    question_text  VARCHAR(255) NOT NULL,
    question_type  VARCHAR(50)  NOT NULL,
    options        TEXT,
    correct_answer VARCHAR(255) NOT NULL,
    image_url      VARCHAR(255),
    difficulty     VARCHAR(50)  NOT NULL,
    lesson_id      BIGINT REFERENCES lessons(id),
    game_id        BIGINT REFERENCES games(id)
);

-- ── Tables that depend on children ────────────────────────────────────────────

CREATE TABLE progress (
    id           BIGSERIAL PRIMARY KEY,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP,
    completed    BOOLEAN   NOT NULL DEFAULT FALSE,
    score        INTEGER,
    completed_at TIMESTAMP,
    child_id     BIGINT    NOT NULL REFERENCES children(id),
    lesson_id    BIGINT    NOT NULL REFERENCES lessons(id),
    CONSTRAINT uq_child_lesson UNIQUE (child_id, lesson_id)
);

CREATE TABLE child_achievements (
    id             BIGSERIAL PRIMARY KEY,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP,
    unlocked_at    TIMESTAMP NOT NULL,
    child_id       BIGINT    NOT NULL REFERENCES children(id),
    achievement_id BIGINT    NOT NULL REFERENCES achievements(id),
    CONSTRAINT uq_child_achievement UNIQUE (child_id, achievement_id)
);

CREATE TABLE streaks (
    id             BIGSERIAL PRIMARY KEY,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP,
    date           DATE      NOT NULL,
    practice_count INTEGER   NOT NULL,
    child_id       BIGINT    NOT NULL REFERENCES children(id),
    CONSTRAINT uq_child_streak_date UNIQUE (child_id, date)
);

CREATE TABLE points_log (
    id         BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    points     INTEGER      NOT NULL,
    reason     VARCHAR(255) NOT NULL,
    child_id   BIGINT       NOT NULL REFERENCES children(id)
);

CREATE TABLE game_scores (
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP,
    score       INTEGER   NOT NULL,
    stars_earned INTEGER  NOT NULL,
    time_spent  INTEGER,
    played_at   TIMESTAMP NOT NULL,
    child_id    BIGINT    NOT NULL REFERENCES children(id),
    game_id     BIGINT    NOT NULL REFERENCES games(id)
);
