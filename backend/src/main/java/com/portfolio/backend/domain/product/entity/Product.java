package com.portfolio.backend.domain.product.entity;

import com.portfolio.backend.common.config.converter.MoneyConverter;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.value.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
public class Product {

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
		if(description == null || description.isBlank()) {
			throw new DomainException("상품설명은 비어있을 수 없습니다.");
		}
		if(thumbnailImageUrl == null || thumbnailImageUrl.isBlank()) {
			throw new DomainException("썸네일이미지주소는 비어있을 수 없습니다.");
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
		stock.increaseStock(quantity);
		if (status == ProductStatus.SOLD_OUT && stock.isAvailable()) {
			active();
		}
	}

	public void decreaseStock(int quantity) {
		stock.decreaseStock(quantity);
		if (status == ProductStatus.ACTIVE && !stock.isAvailable()) {
			soldOut();
		}
	}

	public boolean isAvailable() {
		return status == ProductStatus.ACTIVE && (stock != null && stock.isAvailable());
	}

	public void active() {
		this.status = ProductStatus.ACTIVE;
	}

	public void soldOut() {
		this.status = ProductStatus.SOLD_OUT;
	}

	public void discontinued() {
		this.status = ProductStatus.DISCONTINUED;
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
