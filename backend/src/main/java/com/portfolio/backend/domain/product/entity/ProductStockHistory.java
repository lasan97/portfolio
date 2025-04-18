package com.portfolio.backend.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_histories")
public class ProductStockHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	private Integer previousQuantity;

	private Integer changedQuantity;

	private Integer currentQuantity;

	@Enumerated(EnumType.STRING)
	private StockChangeReason reason;

	@CreatedDate
	private LocalDateTime createdAt;

	@Column(length = 500)
	private String memo;

	public ProductStockHistory(Product product, int previous, int changed, StockChangeReason reason, String memo) {
		this.product = product;
		this.previousQuantity = previous;
		this.changedQuantity = changed;
		this.currentQuantity = previous + changed;
		this.reason = reason;
		this.memo = memo;
	}
}