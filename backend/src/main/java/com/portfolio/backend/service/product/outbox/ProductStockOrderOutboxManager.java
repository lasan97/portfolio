package com.portfolio.backend.service.product.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.common.event.EventPublisher;
import com.portfolio.backend.common.event.ProductStockStatus;
import com.portfolio.backend.common.event.payload.ProductStockReductionEventPayload;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.outbox.ProductStockOrderOutbox;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.product.repository.ProductStockOrderOutboxRepository;
import com.portfolio.backend.domain.product.service.ProductStockManager;
import com.portfolio.backend.service.common.outbox.OutboxStatus;
import com.portfolio.backend.service.order.outbox.event.ProductStockReductionResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStockOrderOutboxManager {

    private final ProductStockOrderOutboxRepository productStockOrderOutboxRepository;
    private final ProductRepository productRepository;
    private final ProductStockManager productStockManager;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public void productStockOrderOutboxProcess(ProductStockOrderOutbox productStockOrderOutbox) {
        log.info("ProductStockOrderOutbox received saga id : {}", productStockOrderOutbox.getSagaId());

        try {
            ProductStockReductionEventPayload payload = getPayload(productStockOrderOutbox.getPayload(), ProductStockReductionEventPayload.class);

            for (ProductStockReductionEventPayload.OrderItem orderItem : payload.getOrderItems()) {
                Product product = productRepository.findByIdAndStatusNot(orderItem.productId(), ProductStatus.DELETED)
                        .orElseThrow(() -> new ResourceNotFoundException("상품이 존재하지 않습니다."));

                productStockManager.sale(product, orderItem.quantity());
            }
            log.info("ProductStockOutbox reduction order id : {}", productStockOrderOutbox.getOrderId());

            productStockOrderOutbox.setProductStockStatus(ProductStockStatus.COMPLETED);
            productStockOrderOutbox.setOutboxStatus(OutboxStatus.COMPLETED);
            productStockOrderOutbox.setProcessedAt(LocalDateTime.now());
            productStockOrderOutboxRepository.save(productStockOrderOutbox);

            eventPublisher.publishEvent(new ProductStockReductionResponseEvent(
                    productStockOrderOutbox.getSagaId(),
                    productStockOrderOutbox.getProductStockStatus()));
        } catch (UnprocessableEntityException | DomainException e) {
            log.error("ProductStockOutbox failed order id : {}", productStockOrderOutbox.getOrderId());

            productStockOrderOutbox.setOutboxStatus(OutboxStatus.FAILED);
            productStockOrderOutbox.setProductStockStatus(ProductStockStatus.FAILED);
            productStockOrderOutbox.setProcessedAt(LocalDateTime.now());

            eventPublisher.publishEvent(new ProductStockReductionResponseEvent(
                    productStockOrderOutbox.getSagaId(),
                    productStockOrderOutbox.getProductStockStatus()));

        } catch (OptimisticLockingFailureException e) {
            // No Op
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB Exception", e.getMessage(), e);
        }
    }

    private <T> T getPayload(String payload, Class<T> outputType) {
        try {
            return objectMapper.readValue(payload, outputType);
        } catch (JsonProcessingException e) {
            log.error("Could not read {} object", outputType.getName(), e);
            throw new DomainException("Could not read " + outputType.getName() + " object", e);
        }
    }
}
