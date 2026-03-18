package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Streak;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.StreakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakRepository streakRepository;
    private final ChildRepository childRepository;

    @Transactional
    public boolean recordActivity(Long childId) {
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
            return false; // Streak was not updated (already counted today)
        }

        // Create new streak record for today
        Streak todayStreak = Streak.builder()
                .child(child)
                .date(today)
                .practiceCount(1)
                .build();
        streakRepository.save(todayStreak);

        // Update child's current streak
        boolean hadActivityYesterday = streakRepository.existsByChildIdAndDate(childId, yesterday);

        if (hadActivityYesterday) {
            // Continue the streak
            child.setCurrentStreak(child.getCurrentStreak() + 1);
        } else {
            // Reset streak to 1 (today is the first day)
            child.setCurrentStreak(1);
        }

        childRepository.save(child);
        return true; // Streak was updated
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
}
