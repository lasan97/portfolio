package com.portfolio.backend.domain.product.entity;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.domain.common.entity.AggregateRoot;
import com.portfolio.backend.domain.product.event.ProductStockChangedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_stocks")
@EntityListeners(AuditingEntityListener.class)
public class ProductStock extends AggregateRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", unique = true)
	private Product product;

	@Column(nullable = false)
	private Integer quantity;

	@Version
	private int version;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	public ProductStock(Product product, Integer quantity) {
		if (product == null) {
			throw new DomainException("상품은 null일 수 없습니다.");
		}
		if (quantity == null || quantity < 0) {
			throw new DomainException("quantity는 0보다 작을 수 없습니다.");
		}
		this.product = product;
		this.quantity = quantity;
	}

	public boolean isAvailable() {
		return quantity > 0;
	}

	public void increase(int amount, StockChangeReason reason) {
		int previousQuantity = this.quantity;

		if (amount < 0) {
			throw new UnprocessableEntityException("증가시킬 수량은 음수일 수 없습니다: " + amount);
		}
		this.quantity += amount;

		registerStockChangedEvent(previousQuantity, amount, reason);
	}

	public void decrease(int amount, StockChangeReason reason) {
		int previousQuantity = this.quantity;

		if (this.quantity < amount) {
			throw new UnprocessableEntityException("재고가 부족합니다.");
		}
		this.quantity -= amount;

		registerStockChangedEvent(previousQuantity, -amount, reason);
	}

	public void adjust(int amount, String memo) {
		int previousQuantity = this.quantity;

		if (amount < 0) {
			throw new UnprocessableEntityException("재고는 0보다 작을 수 없습니다.");
		}
		this.quantity = amount;

		registerStockChangedEvent(previousQuantity, amount - previousQuantity, StockChangeReason.ADJUSTMENT, memo);
	}

	private void registerStockChangedEvent(int previousQuantity, int changedQuantity, StockChangeReason reason) {
		registerStockChangedEvent(previousQuantity, changedQuantity, reason, null);
	}

	private void registerStockChangedEvent(int previousQuantity, int changedQuantity, StockChangeReason reason, String memo) {
		registerEvent(ProductStockChangedEvent.builder()
				.product(product)
				.previousQuantity(previousQuantity)
				.changedQuantity(changedQuantity)
				.memo(memo)
				.reason(reason)
				.transactionDateTime(LocalDateTime.now())
				.build());
	}
}
