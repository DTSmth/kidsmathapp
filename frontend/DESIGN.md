# KidsMathApp Design System

This document captures all design decisions for the KidsMathApp frontend.
All new components must align with these specs before implementation.

---

## Brand Identity

**Personality:** Warm, encouraging, playful. Never punishing. Math is an adventure, not a test.

**Voice:** Direct, child-friendly, positive. Short sentences. Lots of animals and emojis.
- Wrong answers: "Almost!" / "So close!" — NEVER "Wrong!" / "Incorrect!"
- Failed lesson: "Getting there! 💪" — NEVER "You failed"
- Scores: See "Copy Rules" section below.

---

## Color Palette

| Token       | Hex       | Usage                                      |
|-------------|-----------|---------------------------------------------|
| `primary`   | `#4ECDC4` | Teal — primary actions, progress, success  |
| `accent`    | `#FFE66D` | Yellow — stars, achievements, highlights   |
| `coral`     | `#FF6B6B` | Coral — wrong answers (soft), CTAs         |
| `purple`    | `#A084E8` | Purple — topic 4+, variety color           |
| `success`   | `#95D5B2` | Green — correct answers, completed states  |
| `background`| `#FFF9E8` | Cream — page background                    |

**Topic color cycling** (by topic index % 5):
- Index 0: `primary` (teal)
- Index 1: `accent` (yellow)
- Index 2: `coral`
- Index 3: `purple`
- Index 4: `success` (green)

---

## Typography

- **Font family:** Nunito → Comic Sans MS → cursive → sans-serif
- **Body text:** `font-semibold text-gray-700`
- **Headings:** `font-extrabold text-gray-800`
- **Small/meta text:** `text-sm text-gray-500`
- **Score/celebration text:** Large gradient — `bg-gradient-to-r from-primary to-coral bg-clip-text text-transparent`

---

## Component Vocabulary

### Card
`rounded-3xl shadow-lg p-6 border-4 bg-white`
- Color variants: `border-primary`, `border-accent`, `border-coral`, `border-purple`, `border-success`, `border-gray-200`
- Hoverable: `hover:scale-105 hover:shadow-xl cursor-pointer transition-all duration-200`

### Button
`rounded-2xl px-6 py-3 font-bold text-lg hover:scale-105 active:scale-95 transition-all duration-200`
- Disabled: `opacity-50 cursor-not-allowed hover:scale-100`

### Avatar
Circular, `bg-gradient-to-br from-accent-light to-accent`, emoji centered.

### ProgressBar
Gradient fill: `bg-gradient-to-r from-primary via-purple to-coral`
Height variants: sm (`h-2`), md (`h-4`), lg (`h-6`)

### AnimalMascot
4 animals: leo 🦁, ollie 🦉, bella 🐰, max 🐵
4 moods: happy (bounce), excited (pulse), thinking (static), celebrating (bounce + 🎉)
Speech bubble: `bg-white rounded-2xl border-2 border-primary` with CSS triangle pointer

---

## New Components (from MVP plan)

### QuestionCard
`Card` base + state variants:
- Default: `border-gray-200`
- Correct: `border-success bg-success/10` + `bounce-in` animation
- Wrong: `border-coral bg-coral/10` + `shake` animation (already in Tailwind)

### MCAnswerButton
`Button` base, full-width on mobile:
- Unselected: `border-2 border-gray-200 bg-white text-gray-700`
- Selected-correct: `bg-success/20 border-success`
- Selected-wrong: `bg-coral/20 border-coral`

### NumberPad
Layout: 3×3 grid (1-9) + bottom row (⌫, 0, ✓)
- Key style: `rounded-2xl font-bold text-2xl active:scale-95`
- Number keys: `bg-primary text-white`
- Backspace: `bg-coral/20 text-coral`
- Submit ✓: `bg-success text-white` (disabled/gray when input empty)
- **Minimum key size: 56×56px on mobile**
- Input display above pad: `rounded-xl bg-gray-100 text-2xl h-14 text-center`
- Max input: 4 digits
- **Keyboard support on desktop:** typing digits fills input, Enter = submit, Backspace = delete last digit

### QuizProgressDots
`w-3 h-3 rounded-full` dots:
- Empty: `bg-gray-200`
- Current: `bg-accent animate-pulse`
- Completed: `bg-primary`

### AchievementToast
`Card` base: `rounded-3xl border-4 border-accent bg-white w-80 shadow-xl`
- Position: `fixed bottom-6 left-1/2 -translate-x-1/2 z-50`
- Enter: `slide-up` animation (300ms)
- Auto-dismiss: 3 seconds
- Exit: fade out (300ms)
- Tap to dismiss: yes
- Max 2 visible at once; 3rd waits in queue (1.5s delay)
- Content: `[emoji large left] [name bold] ["Unlocked! 🎉" text]`

### StreakFireBadge (Header upgrade)
- 1-2 days: `🔥` static pill (existing bg-coral/20)
- 3-6 days: `🔥🔥` + `animate-pulse`
- 7+ days: `🔥🔥🔥` + fast pulse (`animation-duration: 0.5s`)

### DailyChallengeBanner
`border-2 border-accent bg-accent/20 rounded-2xl px-4 py-3`
- Complete: `✅ You practiced today! Keep it up!`
- Incomplete: `📅 Start today's challenge →` (clickable, leads to first incomplete lesson)

---

## Confetti Spec (CSS-only, no library)

60 particles total:
- Colors: `#4ECDC4`, `#FFE66D`, `#FF6B6B`, `#A084E8`, `#ffffff`
- Shapes: circles (50%), squares rotated 45° (30%), ⭐ emoji (20%)
- Origin: center of screen
- Duration: 2.5s, then fade out and unmount
- Spread: ±40vw horizontal, falls 100vh
- Wrapper: `aria-hidden="true"` (decorative only)

---

## Screen Hierarchies

### Dashboard
1. AnimalMascot + personalized greeting
2. Stats row: stars + streak
3. Daily Challenge banner (small, accent-bordered)
4. Topic grid

### LessonQuiz
1. Progress dots (top bar)
2. Question card (80% of viewport, centered)
3. Answer surface (MC buttons stacked OR NumberPad)
4. Mascot only on: first question intro + wrong answer hint

### LessonComplete (passed)
1. Score + grade label (large, gradient text)
2. Stars earned (animated count-up: "+15 base + 7 bonus")
3. Mascot celebrating + "[Name], you're a math star! 🎉"
4. Achievement toasts (bottom, non-blocking)
5. "Continue" button (primary, prominent)

### LessonComplete (failed)
1. Score: "[68%] — Getting there! 💪"
2. Mascot thinking + "[Name], practice makes perfect!"
3. Brief review: which questions were missed (2-3 items, no shame)
4. "Try Again 🔄" (primary) + "Back to Lessons" (text link)

---

## Copy Rules

### Score Labels
| Score  | Label             | Emoji       |
|--------|-------------------|-------------|
| 100%   | PERFECT!          | 🌟🌟🌟      |
| 90-99% | AMAZING!          | 🌟🌟        |
| 80-89% | GREAT JOB!        | 🌟          |
| 70-79% | NICE WORK!        | ✅          |
| <70%   | Getting there!    | 💪          |

### Wrong Answer Messaging
- Preferred: "Almost!", "So close!", "Good try!", "Nearly there!"
- Never: "Wrong!", "Incorrect!", "Try harder"

### Empty States
| Screen          | Copy                                             |
|-----------------|--------------------------------------------------|
| Topics (empty)  | "No topics yet 🌱 — check back soon!"           |
| Lessons (empty) | "No lessons here yet!"                          |
| Parent (no kids)| "Add your first learner to get started! 🐣"    |
| Achievements    | "Complete lessons to earn your first badge! 🏅" |

---

## Responsive Behavior

**Mobile-first.** Primary viewport: phone (375px–414px).

| Breakpoint | Topics grid    | Lesson list       | Quiz answer buttons |
|------------|----------------|-------------------|---------------------|
| Mobile     | `grid-cols-2`  | Full-width cards  | Full-width stacked  |
| Tablet+    | `grid-cols-3`  | Full-width cards  | Full-width stacked  |
| Desktop    | `grid-cols-4`  | 2-column grid     | Full-width (centered, max-w-md) |

**Quiz on mobile:**
- Header: **minimal quiz bar** — replace full app header with: `[X exit]` left, progress dots center, nothing right. Full header returns after quiz exits.
- Question card: full-width, large text (`text-xl md:text-2xl`)
- Answer buttons: full-width, min 56px height
- NumberPad: centered, min 280px wide

---

## Accessibility

- **Touch targets:** Minimum 56×56px for all interactive elements (exceeds WCAG 44px — kids have wider fingers)
- **Correct/wrong feedback:** Never rely on color alone — use shake animation (wrong) and star burst + scale-up (correct) as shape/motion signals
- **Quiz feedback:** `role="alert"` on the feedback overlay (announces to screen readers)
- **Confetti:** `aria-hidden="true"` — decorative only
- **NumberPad keys:** `aria-label="Number 7"`, `aria-label="Backspace"`, `aria-label="Submit answer"`
- **MC answer buttons:** `aria-label="Answer option: [text]"`
- **Progress dots:** `aria-label="Question 2 of 5"`
- **Achievement toast:** `role="status"` + `aria-live="polite"`

---

## Animation Reference

All animations are defined in `tailwind.config.js`:

| Name              | Usage                                    |
|-------------------|------------------------------------------|
| `fade-in`         | Page/component enter                    |
| `slide-up`        | Achievement toast enter, modal enter    |
| `bounce-in`       | Correct answer ⭐ overlay, achievement  |
| `shake`           | Wrong answer card                        |
| `mascot-bounce`   | Mascot idle animation                   |
| `float`           | Background decorations (Home page)      |
| `twinkle`         | StarCounter, background stars           |

**New animations needed for MVP:**
- `starFly`: ⭐ emoji flies from result card to header StarCounter (translateX+translateY, fade-out, 600ms)
- `confettifall`: particle falls from top to bottom+side (see Confetti Spec)
- `slideLeft`: quiz question card exit on advance (translateX -100%, 200ms)
- `slideInRight`: next question enters (translateX +100% → 0, 200ms)
