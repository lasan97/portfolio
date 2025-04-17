package com.portfolio.backend.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductCategory {
	ELECTRONICS("전자제품"),
	FURNITURE("가구"),
	HOME_APPLIANCE("가전제품"),
	CLOTHING("의류"),
	SHOES("신발"),
	ACCESSORIES("액세서리"),
	BEAUTY("뷰티/화장품"),
	HEALTH("건강/의료용품"),
	SPORTS("스포츠/레저"),
	BABY("유아용품"),
	FOOD("식품"),
	BEVERAGE("음료"),
	BOOKS("도서"),
	STATIONERY("문구/사무용품"),
	KITCHEN("주방용품"),
	BATHROOM("욕실용품"),
	BEDDING("침구류"),
	PETS("반려동물용품"),
	PLANTS("식물/원예용품"),
	DIGITAL_CONTENT("디지털 콘텐츠"),
	TOYS("장난감/취미용품"),
	AUTOMOTIVE("자동차용품"),
	OUTDOOR("아웃도어/캠핑"),
	TRAVEL("여행용품");

	private final String description;
}
