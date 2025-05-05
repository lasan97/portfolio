package com.portfolio.backend.service.order;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.order.entity.Order;
import com.portfolio.backend.domain.order.repository.OrderRepository;
import com.portfolio.backend.service.order.dto.OrderServiceMapper;
import com.portfolio.backend.service.order.dto.OrderServiceRequest;
import com.portfolio.backend.service.order.dto.OrderServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final DomainEventPublisher eventPublisher;

    private final OrderServiceMapper orderServiceMapper;

    @Transactional
    public void createOrder(Long userId, OrderServiceRequest.Create request) {
        Order order = orderServiceMapper.createOrder(userId, request);

        orderRepository.save(order);

        eventPublisher.publishEventsFrom(order);
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
}
