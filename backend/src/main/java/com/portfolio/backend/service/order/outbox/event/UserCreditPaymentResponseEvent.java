package com.portfolio.backend.service.order.outbox.event;

import com.portfolio.backend.common.event.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserCreditPaymentResponseEvent {

    private UUID sagaId;

    private PaymentStatus paymentStatus;
}
