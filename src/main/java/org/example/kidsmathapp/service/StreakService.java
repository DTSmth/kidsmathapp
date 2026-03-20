package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.progress.StreakCalendarDto;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Streak;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.StreakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakRepository streakRepository;
    private final ChildRepository childRepository;

    public record StreakResult(boolean updated, boolean wouldHaveBroken, int previousStreak) {}

    @Transactional
    public StreakResult recordActivity(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found with id: " + childId));

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Optional<Streak> todayStreakOpt = streakRepository.findByChildIdAndDate(childId, today);

        if (todayStreakOpt.isPresent()) {
            // Already recorded today, just increment practice count
            Streak todayStreak = todayStreakOpt.get();
            todayStreak.setPracticeCount(todayStreak.getPracticeCount() + 1);
            streakRepository.save(todayStreak);
            return new StreakResult(false, false, child.getCurrentStreak()); // Streak was not updated (already counted today)
        }

        // Create new streak record for today
        Streak todayStreak = Streak.builder()
                .child(child)
                .date(today)
                .practiceCount(1)
                .dailyBonusClaimed(false)
                .build();
        streakRepository.save(todayStreak);

        // Update child's current streak
        boolean hadActivityYesterday = streakRepository.existsByChildIdAndDate(childId, yesterday);

        if (hadActivityYesterday) {
            // Continue the streak
            child.setCurrentStreak(child.getCurrentStreak() + 1);
            childRepository.save(child);
            return new StreakResult(true, false, child.getCurrentStreak() - 1);
        } else {
            // Streak breaks — reset to 1
            int previousStreak = child.getCurrentStreak();
            child.setCurrentStreak(1);
            childRepository.save(child);
            return new StreakResult(true, true, previousStreak);
        }
    }

    @Transactional
    public void preserveStreak(Long childId, int previousStreak) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found with id: " + childId));
        child.setCurrentStreak(previousStreak + 1); // keep the streak going
        childRepository.save(child);
        log.info("Streak preserved for child {} via shield: {} → {}", childId, previousStreak, previousStreak + 1);
    }

    @Transactional(readOnly = true)
    public int getCurrentStreak(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found with id: " + childId));

        return child.getCurrentStreak();
    }

    @Transactional(readOnly = true)
    public boolean hasActivityToday(Long childId) {
        return streakRepository.existsByChildIdAndDate(childId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public StreakCalendarDto getStreakCalendar(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found with id: " + childId));

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);

        List<Streak> streaks = streakRepository.findByChildIdAndDateBetweenOrderByDateAsc(
                childId, thirtyDaysAgo, today);

        // Build day-by-day for last 30 days
        List<StreakCalendarDto.StreakDayDto> days = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Streak streak = streaks.stream().filter(s -> s.getDate().equals(date)).findFirst().orElse(null);
            days.add(StreakCalendarDto.StreakDayDto.builder()
                    .date(date)
                    .practiced(streak != null && streak.getPracticeCount() > 0)
                    .practiceCount(streak != null ? streak.getPracticeCount() : 0)
                    .dailyBonusClaimed(streak != null && Boolean.TRUE.equals(streak.getDailyBonusClaimed()))
                    .isToday(date.equals(today))
                    .build());
        }

        return StreakCalendarDto.builder()
                .currentStreak(child.getCurrentStreak())
                .longestStreak(child.getCurrentStreak()) // simplified - could track separately
                .days(days)
                .build();
    }
}
