package com.food.ordering.system.order.service.dataaccess.order.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueObject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueObject.StreeAddress;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;

@Component
public class OrderDataAccessMapper {

	public OrderEntity orderToOrderEntity(Order order) {
		OrderEntity orderEntity = OrderEntity.builder()
				.id(order.getId().getValue())
				.customerId(order.getCustomerId().getValue())
				.restaurantId(order.getRestaurantId().getValue())
				.trackingId(order.getTrackingId().getValue())
				.price(order.getPrice().getAmount())
				.orderStatus(order.getOrderStatus())
				.failureMessages(order.getFailureMessages() != null
						? String.join(Order.FAILURE_MESSAGE_DELIMITER, order.getFailureMessages())
						: "")
				.address(deliveryAddressToOrderAddressEntity(order.getDeliveryAddress()))
				.items(orderItemsToOrderItemsEntity(order.getOrderItems()))
				.build();

		orderEntity.getAddress().setOrder(orderEntity);
		orderEntity.getItems().forEach(item -> item.setOrder(orderEntity));

		return orderEntity;
	}

	public Order orderEntityToOrder(OrderEntity orderEntity) {
		return Order.builder()
				.orderId(new OrderId(orderEntity.getId()))
				.customerId(new CustomerId(orderEntity.getCustomerId()))
				.restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
				.trackingId(new TrackingId(orderEntity.getTrackingId()))
				.price(new Money(orderEntity.getPrice()))
				.orderStatus(orderEntity.getOrderStatus())
				.failureMessages(orderEntity.getFailureMessages().isEmpty() ? new ArrayList<String>()
						: new ArrayList<String>(
								Arrays.asList(orderEntity.getFailureMessages().split(Order.FAILURE_MESSAGE_DELIMITER))))
				.deliveryAddress(orderAddressEntityToDeliveryAddress(orderEntity.getAddress()))
				.orderItems(orderItemsEntityToOrderItems(orderEntity.getItems()))
				.build();
	}

	private List<OrderItem> orderItemsEntityToOrderItems(List<OrderItemEntity> items) {
		return items.stream().map((i) -> OrderItem.builder()
				.setOrderItemId(new OrderItemId(i.getId()))
				.setProduct(new Product(new ProductId(i.getProductId())))
				.setQuantity(i.getQuantity())
				.setPrice(new Money(i.getPrice()))
				.setSubTotal(new Money(i.getSubtotal()))
				.build())
				.collect(Collectors.toList());
	}

	private StreeAddress orderAddressEntityToDeliveryAddress(OrderAddressEntity address) {
		return new StreeAddress(address.getId(), address.getStreet(), address.getPostalCode(), address.getCity());
	}

	private List<OrderItemEntity> orderItemsToOrderItemsEntity(List<OrderItem> orderItems) {
		return orderItems.stream().map(orderItem -> OrderItemEntity.builder()
				.id(orderItem.getId().getValue())
				.productId(orderItem.getProduct().getId().getValue())
				.price(orderItem.getPrice().getAmount())
				.quantity(orderItem.getQuantity())
				.subtotal(orderItem.getSubTotal().getAmount())
				.build())
				.collect(Collectors.toList());
	}

	private OrderAddressEntity deliveryAddressToOrderAddressEntity(StreeAddress address) {
		return OrderAddressEntity.builder()
				.id(address.getId())
				.city(address.getCity())
				.postalCode(address.getPostalCode())
				.street(address.getStreet())
				.build();
	}

}
