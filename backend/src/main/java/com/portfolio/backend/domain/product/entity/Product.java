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

		registerStockChangedEvent(0,
				this.stock.getQuantity(),
				StockChangeReason.ADJUSTMENT,
				"상품등록");
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

	public void increaseStock(int quantity) {
		int previousQuantity = stock.getQuantity();

		stock.increaseStock(quantity);
		if (status == ProductStatus.SOLD_OUT && stock.isAvailable()) {
			active();
		}

		registerStockChangedEvent(previousQuantity,
				this.stock.getQuantity(),
				StockChangeReason.RETURN,
				"상품 환불");
	}

	public void decreaseStock(int quantity) {
		int previousQuantity = stock.getQuantity();

		stock.decreaseStock(quantity);
		if (status == ProductStatus.ACTIVE && !stock.isAvailable()) {
			soldOut();
		}

		registerStockChangedEvent(previousQuantity,
				this.stock.getQuantity(),
				StockChangeReason.SALE,
				"상품 판매");
	}

	public void adjustStock(int quantity, String memo) {
		int previousQuantity = stock.getQuantity();

		stock.adjustStock(quantity);
		if (status != ProductStatus.DELETED) {
			if (stock.isAvailable()) {
				active();
			} else {
				soldOut();
			}
		}

		registerStockChangedEvent(previousQuantity,
				this.stock.getQuantity(),
				StockChangeReason.ADJUSTMENT,
				memo);
	}

	public boolean isAvailable() {
		return status == ProductStatus.ACTIVE && (stock != null && stock.isAvailable());
	}

	private void active() {
		this.status = ProductStatus.ACTIVE;
	}

	private void soldOut() {
		this.status = ProductStatus.SOLD_OUT;
	}

	public void delete() {
		this.status = ProductStatus.DELETED;

		int previousQuantity = stock.getQuantity();

		stock.adjustStock(0);

		registerStockChangedEvent(previousQuantity,
				this.stock.getQuantity(),
				StockChangeReason.STOP_SALE,
				"판매 중지");
	}

	public int getDiscountRate() {
		if (originalPrice.isZero()) return 0;

		return originalPrice.subtract(price)
				.multiply(100).getAmount()
				.divide(originalPrice.getAmount(), 0, RoundingMode.FLOOR)
				.intValue();
	}

	private void registerStockChangedEvent(int previousQuantity, int changedQuantity, StockChangeReason reason, String memo) {
		registerEvent(ProductStockChangedEvent.builder()
				.product(this)
				.previousQuantity(previousQuantity)
				.changedQuantity(changedQuantity)
				.memo(memo)
				.reason(reason)
				.transactionDateTime(LocalDateTime.now())
				.build());
	}
}
