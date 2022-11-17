package com.food.ordering.system.order.service.domain.entity;

import java.util.List;
import java.util.UUID;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueObject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueObject.StreeAddress;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;

public class Order extends AggregateRoot<OrderId> {
	private final CustomerId customerId;
	private final RestaurantId restaurantId;
	private final StreeAddress deliveryAddress;
	private final Money price;
	private final List<OrderItem> items;

	private TrackingId trackingId;
	private OrderStatus orderStatus;
	private List<String> failureMessages;

	// Business logic
	public void validateOrder() {
		validateInitialOrder();
		validateTotalPrice();
		validateItemsPrice();
	}

	public void initializeOrder() {
		super.setId(new OrderId(UUID.randomUUID()));
		trackingId = new TrackingId(UUID.randomUUID());
		orderStatus = OrderStatus.PENDING;

		initializeOrderItems();
	}

	public void pay() {
		if (orderStatus != OrderStatus.PENDING) {
			throw new OrderDomainException("Order pay denied. Because it is not in pending state");
		}

		orderStatus = OrderStatus.PAID;
	}

	public void approve() {
		if (orderStatus != OrderStatus.PAID) {
			throw new OrderDomainException("Order approve denied! Because it is not in paid state");
		}

		orderStatus = OrderStatus.APPROVED;
	}

	public void initCancel(List<String> failureMessages) {
		if (orderStatus != OrderStatus.PAID) {
			throw new OrderDomainException("Order initCancel denied! Because it not in paid state");
		}

		orderStatus = OrderStatus.CANCELLING;
		updateFailerMessages(failureMessages);
	}

	public void cancel(List<String> failureMessages) {
		if (orderStatus != OrderStatus.CANCELLING || orderStatus != OrderStatus.PENDING) {
			throw new OrderDomainException("Order cancel denied! Because it is not in cancelling or pending state");
		}

		orderStatus = OrderStatus.CANCELLED;
		updateFailerMessages(failureMessages);
	}

	// Business logic helpers
	private void validateInitialOrder() {
		if (orderStatus != null || getId() != null) {
			throw new OrderDomainException("Order is is not in correct state");
		}
	}

	private void validateTotalPrice() {
		if (price == null || price.isGreaterThanZero() == false) {
			throw new OrderDomainException("Invalid order price. It should be greater than zero");
		}
	}

	private void validateItemsPrice() {
		Money orderItemsTotal = items.stream().map((orderItem) -> {
			validateItemPrice(orderItem);
			return orderItem.getSubTotal();
		}).reduce(Money.ZERO, Money::add);

		if (price.equals(orderItemsTotal) == false) {
			String message = String.format("Total price: %s is not equal to sum of order items: %s",
					price.getAmount(), orderItemsTotal.getAmount());
			throw new OrderDomainException(message);
		}

	}

	private void validateItemPrice(OrderItem orderItem) {
		if (orderItem.isPriceValid() == false) {
			String message = String.format("Order item price: %s is not valid for product: %s",
					orderItem.getPrice().getAmount(), orderItem.getProduct().getId().getValue());

			throw new OrderDomainException(message);
		}
	}

	private void initializeOrderItems() {
		long itemId = 1;

		for (OrderItem orderItem : items) {
			orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
		}
	}

	private void updateFailerMessages(List<String> failureMessages) {
		if (this.failureMessages != null && failureMessages != null) {
			this.failureMessages.addAll(failureMessages.stream().filter((m -> m.isEmpty() == false)).toList());
		}

		if (this.failureMessages == null) {
			this.failureMessages = failureMessages.stream().filter((m -> m.isEmpty() == false)).toList();
		}
	}

	// Constructor, setters, and getters
	private Order(Builder builder) {
		super.setId(builder.orderId);

		this.customerId = builder.customerId;
		this.restaurantId = builder.restaurantId;
		this.deliveryAddress = builder.deliveryAddress;
		this.price = builder.price;
		this.items = builder.orderItems;
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
		return items;
	}

	public TrackingId getTrackingId() {
		return trackingId;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public List<String> getFailureMessages() {
		return failureMessages;
	}

	// Builder
	public static class Builder {
		private OrderId orderId;
		private CustomerId customerId;
		private RestaurantId restaurantId;
		private StreeAddress deliveryAddress;
		private Money price;
		private List<OrderItem> orderItems;

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

		public Order build() {
			return new Order(this);
		}
	}

}
