package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.entity.Subscription;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.example.kidsmathapp.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaywallEnforcer {

    private static final int FREE_DAILY_LIMIT = 3;

    private final SubscriptionRepository subscriptionRepository;
    private final ProgressRepository progressRepository;

    public boolean canStartLesson(Long userId, Long childId) {
        // Premium users: always allowed
        Optional<Subscription> sub = subscriptionRepository.findByUserId(userId);
        if (sub.isPresent() && sub.get().isPremium()) return true;

        // Free: count lessons completed today for this child
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long completedToday = progressRepository.countByChildIdAndCompletedTrueAndCompletedAtAfter(childId, startOfDay);
        return completedToday < FREE_DAILY_LIMIT;
    }
}
