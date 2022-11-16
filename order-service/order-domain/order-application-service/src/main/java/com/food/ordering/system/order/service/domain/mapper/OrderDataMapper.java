package com.food.ordering.system.order.service.domain.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
// import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.valueObject.StreeAddress;

@Component
public class OrderDataMapper {
	public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
		return Restaurant.builder()
				.withRestaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
				.withProducts(
						createOrderCommand.getOrderItems().stream().map((item) -> new Product(new ProductId(item.getProductId())))
								.collect(Collectors.toList()))
				.build();
	}

	public Order createOrderToOrder(CreateOrderCommand createOrderCommand) {
		return Order.builder()
				.customerId(new CustomerId(createOrderCommand.getCustomerId()))
				.restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
				.deliveryAddress(orderAddressToStreeAddress(createOrderCommand.getAddress()))
				.price(new Money(createOrderCommand.getPrice()))
				.orderItems(orderItemToOrderItemEntities(createOrderCommand.getOrderItems()))
				.build();

	}

	public CreateOrderResponse orderToCreateOrderResponse(Order order) {
		return CreateOrderResponse.builder()
				.orderTrackingId(order.getTrackingId().getValue())
				.orderStatus(order.getOrderStatus())
				.build();
	}

	public StreeAddress orderAddressToStreeAddress(OrderAddress address) {
		return new StreeAddress(
				UUID.randomUUID(),
				address.getStreet(),
				address.getPostalCode(),
				address.getCity());
	}

	public TrackOrderResponse orderToTrackOrderResponse(Order order) {
		return TrackOrderResponse.builder()
				.orderTrackingId(order.getTrackingId().getValue())
				.orderStatus(order.getOrderStatus())
				.failureMessages(order.getFailureMessages())
				.build();
	}

	public Order paymentResponseToOrder(PaymentResponse paymentResponse) {
		return Order.builder()
				.orderId(new OrderId(UUID.fromString(paymentResponse.getOrderId())))
				.customerId(new CustomerId(UUID.fromString(paymentResponse.getCustomerId())))
				.price(new Money(paymentResponse.getPrice()))
				.build();
	}

	private List<OrderItem> orderItemToOrderItemEntities(
			@NotNull List<com.food.ordering.system.order.service.domain.dto.create.OrderItem> orderItems) {
		return orderItems.stream()
				.map((item) -> OrderItem.builder()
						.setProduct(new Product(new ProductId(item.getProductId())))
						.setQuantity(item.getQuantity())
						.setPrice(new Money(item.getPrice()))
						.setSubTotal(new Money(item.getSubTotal()))
						.build())
				.collect(Collectors.toList());

	}

}
