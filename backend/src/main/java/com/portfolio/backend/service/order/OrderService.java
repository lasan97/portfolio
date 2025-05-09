package com.portfolio.backend.service.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.portfolio.backend.common.event.payload.OrderPaymentEventPayload;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.common.outbox.SagaType;
import com.portfolio.backend.domain.order.entity.Order;
import com.portfolio.backend.domain.order.outbox.PaymentOutbox;
import com.portfolio.backend.domain.order.repository.OrderRepository;
import com.portfolio.backend.domain.order.repository.PaymentOutboxRepository;
import com.portfolio.backend.domain.common.outbox.SagaStatus;
import com.portfolio.backend.service.order.dto.OrderServiceMapper;
import com.portfolio.backend.service.order.dto.OrderServiceRequest;
import com.portfolio.backend.service.order.dto.OrderServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ProductCartRepository productCartRepository;

    private final OrderServiceMapper orderServiceMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createOrder(Long userId, OrderServiceRequest.Create request) {
        Order order = orderServiceMapper.createOrder(userId, request);

        orderRepository.save(order);

        ProductCart productCart = productCartRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("장바구니가 존재하지 않습니다."));
        productCart.removeAllItem();

        OrderPaymentEventPayload payload = OrderPaymentEventPayload.builder()
                .userId(userId)
                .price(order.getTotalPrice().getAmount())
                .build();

        paymentOutboxRepository.save(PaymentOutbox.builder()
                .id(UlidCreator.getUlid().toUuid())
                .sagaId(UlidCreator.getUlid().toUuid())
                .orderId(order.getId())
                .sagaType(SagaType.ORDER)
                .createdAt(LocalDateTime.now())
                .payload(createPayload(order.getId(), payload))
                .orderStatus(order.getOrderStatus())
                .sagaStatus(SagaStatus.STARTED)
                .build());
    }

    @Transactional
    public void cancel(Long userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("주문이 존재하지 않습니다."));

        order.canceling();

        OrderPaymentEventPayload payload = OrderPaymentEventPayload.builder()
                .userId(userId)
                .price(order.getTotalPrice().getAmount())
                .build();

        paymentOutboxRepository.save(PaymentOutbox.builder()
                .id(UlidCreator.getUlid().toUuid())
                .sagaId(UlidCreator.getUlid().toUuid())
                .orderId(order.getId())
                .sagaType(SagaType.ORDER_CANCELING)
                .createdAt(LocalDateTime.now())
                .payload(createPayload(order.getId(), payload))
                .orderStatus(order.getOrderStatus())
                .sagaStatus(SagaStatus.STARTED)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<OrderServiceResponse.Get> getPage(Long userId, Pageable pageable) {
        return orderRepository.findAllByUserId(userId, pageable)
                .map(orderServiceMapper::toGet);
    }

    @Transactional(readOnly = true)
    public OrderServiceResponse.Get get(Long userId, String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new ResourceNotFoundException("주문이 존재하지 않습니다."));

        return orderServiceMapper.toGet(order);
    }

    private String createPayload(UUID orderId, OrderPaymentEventPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("OrderApprovalEventPayload를 만드는데 실패했습니다. eventId: {}", orderId.toString(), e);
            throw new DomainException("OrderApprovalEventPayload를 만드는데 실패했습니다. eventId :" + orderId.toString(), e);
        }
    }
}
