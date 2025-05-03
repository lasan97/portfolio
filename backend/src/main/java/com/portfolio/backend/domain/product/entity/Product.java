package com.portfolio.backend.domain.product.entity;

import com.portfolio.backend.common.config.converter.MoneyConverter;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.entity.AggregateRoot;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.event.ProductStockChangedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class Product extends AggregateRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Convert(converter = MoneyConverter.class)
	private Money originalPrice;

	@Column(nullable = false)
	@Convert(converter = MoneyConverter.class)
	private Money price;

	@Column(length = 1000)
	private String description;

	private String thumbnailImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProductCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProductStatus status;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	@Getter(AccessLevel.NONE)
	@OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private ProductStock stock;

	@Builder
	public Product(String name, Money originalPrice, Money price, String description,
				   String thumbnailImageUrl, ProductCategory category, Integer stock) {
		this.name = name;
		this.originalPrice = originalPrice;
		this.price = price;
		this.description = description;
		this.thumbnailImageUrl = thumbnailImageUrl;
		this.category = category;
		this.stock = new ProductStock(this, stock);
		if(this.stock.isAvailable()) {
			this.status = ProductStatus.ACTIVE;
		} else {
			this.status = ProductStatus.SOLD_OUT;
		}

		validation();

		registerEvent(ProductStockChangedEvent.builder()
				.product(this)
				.previousQuantity(0)
				.changedQuantity(getStockQuantity())
				.memo("상품 등록")
				.reason(StockChangeReason.ADJUSTMENT)
				.transactionDateTime(LocalDateTime.now())
				.build());
	}

	private void validation() {
		if(name == null || name.isBlank()){
			throw new DomainException("상품명은 비어있을 수 없습니다.");
		}
		if(originalPrice == null){
			throw new DomainException("원가는 null일 수 없습니다.");
		}
		if(price == null){
			throw new DomainException("판매가는 null일 수 없습니다.");
		}
		if (originalPrice.isLessThan(price)) {
			throw new DomainException("원가는 판매가 보다 작을 수 없습니다.");
		}
		if(category == null){
			throw new DomainException("카테고리는 null일 수 없습니다.");
		}
	}

	public void update(String name, Money originalPrice, Money price, String description,
					   String thumbnailImageUrl, ProductCategory category) {
		this.name = name;
		this.originalPrice = originalPrice;
		this.price = price;
		this.description = description;
		this.thumbnailImageUrl = thumbnailImageUrl;
		this.category = category;

		validation();
	}

	public Integer getStockQuantity() {
		return this.stock.getQuantity();
	}

	public boolean isAvailable() {
		return status == ProductStatus.ACTIVE && stock.isAvailable();
	}

	public void active() {
		if (this.status == ProductStatus.DELETED) {
			throw new DomainException("삭제된 상품의 상태를 변경할 수 없습니다.");
		}
		this.status = ProductStatus.ACTIVE;
	}

	public void soldOut() {
		if (this.status == ProductStatus.DELETED) {
			throw new DomainException("삭제된 상품의 상태를 변경할 수 없습니다.");
		}
		this.status = ProductStatus.SOLD_OUT;
	}

	public void delete() {
		this.status = ProductStatus.DELETED;
	}

	public int getDiscountRate() {
		if (originalPrice.isZero()) return 0;

		return originalPrice.subtract(price)
				.multiply(100).getAmount()
				.divide(originalPrice.getAmount(), 0, RoundingMode.FLOOR)
				.intValue();
	}
}
