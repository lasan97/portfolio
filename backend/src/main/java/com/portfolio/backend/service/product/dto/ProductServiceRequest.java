package com.portfolio.backend.service.product.dto;

import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.product.entity.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProductServiceRequest {

    public record Create(
            @NotBlank(message = "상품명은 필수입니다.")
            String name,

            @NotNull(message = "원가는 필수입니다.")
            Money originalPrice,

            @NotNull(message = "판매가는 필수입니다.")
            Money price,

            @NotBlank(message = "상품 설명은 필수입니다.")
            String description,

            @NotBlank(message = "썸네일 이미지 URL은 필수입니다.")
            String thumbnailImageUrl,

            @NotNull(message = "카테고리는 필수입니다.")
            ProductCategory category,

            @NotNull(message = "재고 수량은 필수입니다.")
            @Positive(message = "재고 수량은 양수여야 합니다.")
            Integer stock
    ) {
    }

    public record Update(
            @NotBlank(message = "상품명은 필수입니다.")
            String name,

            @NotNull(message = "원가는 필수입니다.")
            Money originalPrice,

            @NotNull(message = "판매가는 필수입니다.")
            Money price,

            @NotBlank(message = "상품 설명은 필수입니다.")
            String description,

            @NotBlank(message = "썸네일 이미지 URL은 필수입니다.")
            String thumbnailImageUrl,

            @NotNull(message = "카테고리는 필수입니다.")
            ProductCategory category
    ) {
    }

    public record AdjustStock(
            @NotNull(message = "수량은 필수입니다.")
            Integer quantity,

            @NotNull(message = "재고 변경 사유는 필수입니다.")
            StockChangeReason reason,

            String memo
    ) {
    }
}
