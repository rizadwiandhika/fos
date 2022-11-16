package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.ProductId;

public class Product extends BaseEntity<ProductId> {
	private final String name;
	private final Money price;

	public Product(ProductId id, String name, Money price) {
		super.setId(id);

		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public Money getPrice() {
		return price;
	}

}
