package com.portfolio.backend.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_histories")
@EntityListeners(AuditingEntityListener.class)
public class ProductStockHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	@Column(nullable = false)
	private Integer previousQuantity;

	@Column(nullable = false)
	private Integer changedQuantity;

	@Column(nullable = false)
	private Integer currentQuantity;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private StockChangeReason reason;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(length = 500)
	private String memo;

	@Builder
	public ProductStockHistory(Product product, int previous, int changed, StockChangeReason reason, String memo) {
		this.product = product;
		this.previousQuantity = previous;
		this.changedQuantity = changed;
		this.currentQuantity = previous + changed;
		this.reason = reason;
		this.memo = memo;
	}
}