package com.portfolio.backend.domain.order.entity;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.portfolio.backend.common.config.converter.MoneyConverter;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.common.entity.AggregateRoot;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.order.value.DeliveryInfo;
import com.portfolio.backend.domain.order.value.OrderItem;
import com.portfolio.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends AggregateRoot {

    @Id
    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Embedded
    private DeliveryInfo deliveryInfo;

    @Column(nullable = false)
    @Convert(converter = MoneyConverter.class)
    private Money totalPrice;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "item_seq")
    private List<OrderItem> orderItems;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Order(User user, Money totalPrice, List<OrderItem> orderItems, DeliveryInfo deliveryInfo) {
        Ulid ulid = UlidCreator.getUlid();
        this.id = ulid.toUuid();
        this.user = user;
        this.totalPrice = totalPrice;
        this.orderItems = orderItems;
        this.deliveryInfo = deliveryInfo;
        this.orderStatus = OrderStatus.PENDING_STOCK_REDUCTION;
        this.createdAt = LocalDateTime.ofInstant(ulid.getInstant(), ZoneId.of("Asia/Seoul"));

        validation();
    }

    private void validation() {
        if (user == null) {
            throw new DomainException("주문자는 비어있을 수 없습니다.");
        }
        if (totalPrice == null) {
            throw new DomainException("주문 금액은 비어있을 수 없습니다.");
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new DomainException("주문 상품은 비어있을 수 없습니다.");
        }
        if (deliveryInfo == null) {
            throw new DomainException("주문 정보는 비어있을 수 없습니다.");
        }

        Money itemTotalPrice = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money.zero(), Money::add);

        if (!totalPrice.equals(itemTotalPrice)) {
            throw new UnprocessableEntityException("주문 금액이 일치하지 않습니다.");
        }
    }

    public void completedStockReduction() {
        if (this.orderStatus != OrderStatus.PENDING_STOCK_REDUCTION) {
            throw new DomainException("주문 상태가 올바르지 않습니다.");
        }
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
    }

    public void paymentCompleted() {
        if (this.orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new DomainException("주문 상태가 올바르지 않습니다.");
        }
        this.orderStatus = OrderStatus.ORDERED;
    }

    public void failed() {
        if (this.orderStatus == OrderStatus.ORDERED) {
            throw new DomainException("주문 상태가 올바르지 않습니다.");
        }
        this.orderStatus = OrderStatus.FAILED;
    }

    public void cancel() {
        if (this.orderStatus != OrderStatus.ORDERED) {
            throw new DomainException("주문 상태가 올바르지 않습니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }
}
