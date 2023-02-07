package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.ProductId;

public class Product extends BaseEntity<ProductId> {

	private String name;
	private Money price;
	private final int quantity;
	private boolean available;

	public void updateWithConfirmedNamePriceAndAvailability(String name, Money price, boolean available) {
		this.name = name;
		this.price = price;
		this.available = available;
	}

	private Product(Builder builder) {
		super.setId(builder.productId);
		this.name = builder.name;
		this.price = builder.price;
		this.quantity = builder.quantity;
		this.available = builder.available;
	}

	public String getName() {
		return name;
	}

	public Money getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public boolean isAvailable() {
		return available;
	}

	public static Builder builder() {
		return new Builder();
	}

	// Create a builder for this class
	public static class Builder {
		private ProductId productId;
		private String name;
		private Money price;
		private int quantity;
		private boolean available;

		public Builder setProductId(ProductId productId) {
			this.productId = productId;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setPrice(Money price) {
			this.price = price;
			return this;
		}

		public Builder setQuantity(int quantity) {
			this.quantity = quantity;
			return this;
		}

		public Builder setAvailable(boolean available) {
			this.available = available;
			return this;
		}

		public Product build() {
			return new Product(this);
		}
	}
}
