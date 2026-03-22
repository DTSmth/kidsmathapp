package org.example.kidsmathapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Subscription extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private String status = "FREE"; // FREE | PREMIUM | LAPSED

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_sub_id")
    private String stripeSubId;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    public boolean isPremium() {
        return "PREMIUM".equals(status) && (periodEnd == null || periodEnd.isAfter(LocalDateTime.now()));
    }
}
