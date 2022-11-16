package com.food.ordering.system.order.service.domain.entity;

import java.util.List;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.order.service.domain.valueObject.StreeAddress;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;

public class Order extends AggregateRoot<OrderId> {
	private final CustomerId customerId;
	private final RestaurantId restaurantId;
	private final StreeAddress deliveryAddress;
	private final Money price;
	private final List<OrderItem> orderItems;

	private TrackingId trackingId;
	private OrderStatus orderStatus;
	private List<String> failerMessages;

	private Order(Builder builder) {
		super.setId(builder.orderId);

		this.customerId = builder.customerId;
		this.restaurantId = builder.restaurantId;
		this.deliveryAddress = builder.deliveryAddress;
		this.price = builder.price;
		this.orderItems = builder.orderItems;
		this.trackingId = builder.trackingId;
		this.orderStatus = builder.orderStatus;
		this.failerMessages = builder.failerMessages;
	}

	public static Builder builder() {
		return new Builder();
	}

	public CustomerId getCustomerId() {
		return customerId;
	}

	public RestaurantId getRestaurantId() {
		return restaurantId;
	}

	public StreeAddress getDeliveryAddress() {
		return deliveryAddress;
	}

	public Money getPrice() {
		return price;
	}

	public List<OrderItem> getOrderItems() {
		return orderItems;
	}

	public TrackingId getTrackingId() {
		return trackingId;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public List<String> getFailerMessages() {
		return failerMessages;
	}

	// Builder
	public static class Builder {
		private OrderId orderId;
		private CustomerId customerId;
		private RestaurantId restaurantId;
		private StreeAddress deliveryAddress;
		private Money price;
		private List<OrderItem> orderItems;

		private TrackingId trackingId;
		private OrderStatus orderStatus;
		private List<String> failerMessages;

		private Builder() {
		}

		public Builder orderId(OrderId orderId) {
			this.orderId = orderId;
			return this;
		}

		public Builder customerId(CustomerId customerId) {
			this.customerId = customerId;
			return this;
		}

		public Builder restaurantId(RestaurantId restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}

		public Builder deliveryAddress(StreeAddress deliveryAddress) {
			this.deliveryAddress = deliveryAddress;
			return this;
		}

		public Builder price(Money price) {
			this.price = price;
			return this;
		}

		public Builder orderItems(List<OrderItem> orderItems) {
			this.orderItems = orderItems;
			return this;
		}

		public Builder trackingId(TrackingId trackingId) {
			this.trackingId = trackingId;
			return this;
		}

		public Builder orderStatus(OrderStatus orderStatus) {
			this.orderStatus = orderStatus;
			return this;
		}

		public Builder failerMessages(List<String> failerMessages) {
			this.failerMessages = failerMessages;
			return this;
		}

		public Order build() {
			return new Order(this);
		}
	}

}
