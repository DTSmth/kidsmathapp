package org.example.kidsmathapp.service;

import org.example.kidsmathapp.entity.Subscription;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.example.kidsmathapp.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaywallEnforcerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private PaywallEnforcer paywallEnforcer;

    private static final Long USER_ID = 1L;
    private static final Long CHILD_ID = 10L;

    @BeforeEach
    void setUp() {
        // Default: no subscription
        when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
    }

    @Test
    void premium_user_always_can_start_lesson() {
        Subscription premiumSub = Subscription.builder()
                .status("PREMIUM")
                .periodEnd(LocalDateTime.now().plusDays(30))
                .build();
        when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(premiumSub));

        assertThat(paywallEnforcer.canStartLesson(USER_ID, CHILD_ID)).isTrue();
    }

    @Test
    void free_user_with_zero_lessons_today_can_start() {
        when(progressRepository.countByChildIdAndCompletedTrueAndCompletedAtAfter(eq(CHILD_ID), any(LocalDateTime.class)))
                .thenReturn(0L);

        assertThat(paywallEnforcer.canStartLesson(USER_ID, CHILD_ID)).isTrue();
    }

    @Test
    void free_user_with_three_lessons_today_cannot_start() {
        when(progressRepository.countByChildIdAndCompletedTrueAndCompletedAtAfter(eq(CHILD_ID), any(LocalDateTime.class)))
                .thenReturn(3L);

        assertThat(paywallEnforcer.canStartLesson(USER_ID, CHILD_ID)).isFalse();
    }

    @Test
    void free_user_with_two_lessons_today_can_start() {
        when(progressRepository.countByChildIdAndCompletedTrueAndCompletedAtAfter(eq(CHILD_ID), any(LocalDateTime.class)))
                .thenReturn(2L);

        assertThat(paywallEnforcer.canStartLesson(USER_ID, CHILD_ID)).isTrue();
    }

    @Test
    void lapsed_subscription_user_is_treated_as_free() {
        Subscription lapsedSub = Subscription.builder()
                .status("LAPSED")
                .periodEnd(LocalDateTime.now().minusDays(1))
                .build();
        when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(lapsedSub));
        when(progressRepository.countByChildIdAndCompletedTrueAndCompletedAtAfter(eq(CHILD_ID), any(LocalDateTime.class)))
                .thenReturn(3L);

        assertThat(paywallEnforcer.canStartLesson(USER_ID, CHILD_ID)).isFalse();
    }

    @Test
    void expired_premium_subscription_is_treated_as_not_premium() {
        // Premium but period_end in the past
        Subscription expiredPremium = Subscription.builder()
                .status("PREMIUM")
                .periodEnd(LocalDateTime.now().minusDays(1))
                .build();
        when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(expiredPremium));
        when(progressRepository.countByChildIdAndCompletedTrueAndCompletedAtAfter(eq(CHILD_ID), any(LocalDateTime.class)))
                .thenReturn(3L);

        // isPremium() returns false because periodEnd is in the past
        assertThat(paywallEnforcer.canStartLesson(USER_ID, CHILD_ID)).isFalse();
    }
}
