# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

KidsMathApp is a full-stack math learning platform for kids. It consists of a **Spring Boot backend** (Java 17, Maven) and a **React + TypeScript frontend** (Vite, Tailwind CSS).

# KidsMathApp - Development Rules

## gstack Environment
Use the following gstack skills located in `~/.claude/skills/` for specialized tasks.
Always use the `/browse` skill from gstack for all web browsing; **never** use `mcp__claude-in-chrome__*` tools.

### Available gstack Skills:
- /office-hours, /plan-ceo-review, /plan-eng-review, /plan-design-review
- /design-consultation, /review, /ship, /browse, /qa, /qa-only
- /design-review, /setup-browser-cookies, /retro, /debug, /document-release

## Commands

### Backend
```bash
mvn spring-boot:run       # Run with Docker Compose (auto-starts PostgreSQL)
mvn clean package         # Build JAR
mvn test                  # Run tests
```

### Frontend
```bash
cd frontend
npm run dev               # Dev server (default: http://localhost:5173)
npm run build             # Production build
npm run lint              # ESLint
npm run preview           # Preview production build
```

### Database
The backend uses Docker Compose to start PostgreSQL automatically (`compose.yaml`). Credentials: `myuser`/`secret`, DB: `mydatabase`, port `5432`.

## Architecture

### Backend (Spring Boot 4.0.3 / Java 17)

Layered REST API under `src/main/java/org/example/kidsmathapp/`:
- `controller/` — REST endpoints at `/api/v1/*`
- `service/` — Business logic
- `repository/` — Spring Data JPA repositories
- `entity/` — JPA domain models; `entity/enums/` for enum types
- `dto/` — Request/response objects, organized by domain (`auth/`, `child/`, `content/`, `progress/`)
- `security/` — JWT filter (`JwtAuthenticationFilter`), token provider, `CustomUserDetailsService`
- `config/` — `SecurityConfig`, `CorsConfig`, `DataSeeder` (seeds initial data on startup)
- `exception/` — `ApiException` + `GlobalExceptionHandler`
- `mapper/` — Manual DTO↔Entity mappers (e.g., `ChildMapper`)

**All API responses use the `ApiResponse<T>` wrapper:** `{ success, data, message }`.

**Authentication:** JWT tokens, 24-hour expiration, stored in `localStorage` on the frontend. Stateless session (no server-side sessions).

**Key domain entities:** `User` (parents) → `Child` (learners with `avatarId`, `gradeLevel`, `totalStars`) → `Topic` → `Lesson` → `Question` → `Progress`. Supporting: `Achievement`, `ChildAchievement`, `Streak`, `PointsLog`, `Game`, `GameScore`.

### Frontend (React 19 + TypeScript + Vite)

Source under `frontend/src/`:
- `context/` — `AuthContext` (JWT + user state) and `ChildContext` (selected child profile)
- `hooks/` — `useAuth`, `useChild` for context consumption
- `pages/` — `Home`, `Login`, `Register`, `ChildSelect`, `Dashboard`
- `components/` — Organized by feature: `common/`, `layout/`, `child/`, `characters/`
- `services/api.ts` — Axios instance with JWT interceptor (attaches `Authorization: Bearer <token>`, handles 401 logout)
- `types/index.ts` — Shared TypeScript interfaces

**Routing:** React Router v7. Two route guard types: `ProtectedRoute` (must be authenticated) and `ChildProtectedRoute` (must also have a child selected). Route: `/` → `/login` / `/register` → `/select-child` → `/dashboard`.

**State management:** Context API only (no Redux). `AuthContext` persists JWT in `localStorage`.

### Styling

Tailwind CSS v4 with a custom kid-friendly theme defined in `tailwind.config.js`:
- Colors: primary teal `#4ECDC4`, accent yellow `#FFE66D`, coral `#FF6B6B`, purple `#A084E8`, background cream `#FFF9E8`
- Custom animations: `fade-in`, `slide-up`, `bounce-in`, `shake`, `mascot-bounce`, `float`, `twinkle`
- Font: Nunito (primary) / Comic Sans MS (fallback)
- Grade levels mapped to Tailwind colors: kindergarten→pink, grade1→purple … grade5→coral
