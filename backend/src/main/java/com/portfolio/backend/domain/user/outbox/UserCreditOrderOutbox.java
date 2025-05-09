package com.portfolio.backend.domain.user.outbox;

import com.portfolio.backend.common.event.PaymentStatus;
import com.portfolio.backend.domain.common.outbox.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user_credit_order_outbox")
@Entity
public class UserCreditOrderOutbox {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID sagaId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}
