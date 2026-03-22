package org.example.kidsmathapp.service;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.entity.User;
import org.example.kidsmathapp.repository.SubscriptionRepository;
import org.example.kidsmathapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Value("${stripe.api-key:sk_test_placeholder}")
    private String stripeApiKey;

    @Value("${stripe.price-id:price_placeholder}")
    private String stripePriceId;

    @Transactional
    public void handleStripeEvent(Event event) {
        log.info("Processing Stripe event: {}", event.getType());

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> optionalObject = deserializer.getObject();

        if (optionalObject.isEmpty()) {
            log.warn("Could not deserialize Stripe event object for event: {}", event.getId());
            return;
        }

        try {
            switch (event.getType()) {
                case "customer.subscription.created" -> {
                    Subscription stripeSub = (Subscription) optionalObject.get();
                    handleSubscriptionCreated(stripeSub);
                }
                case "customer.subscription.updated" -> {
                    Subscription stripeSub = (Subscription) optionalObject.get();
                    handleSubscriptionUpdated(stripeSub);
                }
                case "customer.subscription.deleted" -> {
                    Subscription stripeSub = (Subscription) optionalObject.get();
                    handleSubscriptionDeleted(stripeSub);
                }
                default -> log.info("Ignoring unhandled Stripe event type: {}", event.getType());
            }
        } catch (Exception e) {
            // Never throw — log and ignore to prevent webhook retry loops
            log.error("Error handling Stripe event {}: {}", event.getType(), e.getMessage());
        }
    }

    private void handleSubscriptionCreated(Subscription stripeSub) {
        String customerId = stripeSub.getCustomer();
        org.example.kidsmathapp.entity.Subscription sub = findOrCreateByCustomerId(customerId, stripeSub.getId());
        sub.setStatus("PREMIUM");
        sub.setStripeSubId(stripeSub.getId());
        if (stripeSub.getCurrentPeriodEnd() != null) {
            sub.setPeriodEnd(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd()), ZoneId.systemDefault()));
        }
        subscriptionRepository.save(sub);
        log.info("Subscription created for customer: {}", customerId);
    }

    private void handleSubscriptionUpdated(Subscription stripeSub) {
        subscriptionRepository.findByStripeSubId(stripeSub.getId()).ifPresentOrElse(sub -> {
            String stripeStatus = stripeSub.getStatus();
            sub.setStatus(mapStripeStatus(stripeStatus));
            if (stripeSub.getCurrentPeriodEnd() != null) {
                sub.setPeriodEnd(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd()), ZoneId.systemDefault()));
            }
            subscriptionRepository.save(sub);
            log.info("Subscription updated: {}", stripeSub.getId());
        }, () -> log.warn("Subscription not found for update: {}", stripeSub.getId()));
    }

    private void handleSubscriptionDeleted(Subscription stripeSub) {
        subscriptionRepository.findByStripeSubId(stripeSub.getId()).ifPresentOrElse(sub -> {
            sub.setStatus("LAPSED");
            subscriptionRepository.save(sub);
            log.info("Subscription lapsed: {}", stripeSub.getId());
        }, () -> log.warn("Subscription not found for deletion: {}", stripeSub.getId()));
    }

    private String mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active", "trialing" -> "PREMIUM";
            case "past_due", "unpaid" -> "LAPSED";
            default -> "FREE";
        };
    }

    private org.example.kidsmathapp.entity.Subscription findOrCreateByCustomerId(String customerId, String stripeSubId) {
        return subscriptionRepository.findByStripeCustomerId(customerId).orElseGet(() -> {
            // Try to find a user with this customer ID — for MVP, create a free sub
            org.example.kidsmathapp.entity.Subscription newSub = org.example.kidsmathapp.entity.Subscription.builder()
                    .stripeCustomerId(customerId)
                    .stripeSubId(stripeSubId)
                    .status("FREE")
                    .build();
            return newSub;
        });
    }

    @Transactional(readOnly = true)
    public org.example.kidsmathapp.entity.Subscription getSubscriptionForUser(Long userId) {
        return subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> org.example.kidsmathapp.entity.Subscription.builder()
                        .status("FREE")
                        .build());
    }

    @Transactional
    public String createCheckoutSession(Long userId, String successUrl, String cancelUrl) {
        try {
            com.stripe.Stripe.apiKey = stripeApiKey;

            User user = userRepository.findById(userId).orElseThrow();
            org.example.kidsmathapp.entity.Subscription sub = subscriptionRepository.findByUserId(userId)
                    .orElse(null);

            com.stripe.param.checkout.SessionCreateParams.Builder builder =
                    com.stripe.param.checkout.SessionCreateParams.builder()
                            .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                            .setSuccessUrl(successUrl != null ? successUrl : "https://kidsmathapp.app/dashboard?upgraded=true")
                            .setCancelUrl(cancelUrl != null ? cancelUrl : "https://kidsmathapp.app/dashboard")
                            .setCustomerEmail(user.getEmail())
                            .addLineItem(
                                    com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                                            .setPrice(stripePriceId)
                                            .setQuantity(1L)
                                            .build()
                            );

            if (sub != null && sub.getStripeCustomerId() != null) {
                builder.setCustomer(sub.getStripeCustomerId());
            }

            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(builder.build());
            return session.getUrl();
        } catch (Exception e) {
            log.error("Failed to create Stripe checkout session: {}", e.getMessage());
            throw new RuntimeException("Could not create checkout session: " + e.getMessage());
        }
    }
}
