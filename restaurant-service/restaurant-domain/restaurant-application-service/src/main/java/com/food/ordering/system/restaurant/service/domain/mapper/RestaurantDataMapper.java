package com.food.ordering.system.restaurant.service.domain.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;

@Component
public class RestaurantDataMapper {

	public Restaurant restaurantApprovalRequestToRestaurant(
			RestaurantApprovalRequest restaurantApprovalRequest) {
		return Restaurant.builder()
				.setRestaurantId(new RestaurantId(UUID.fromString(restaurantApprovalRequest.getRestaurantId())))
				.setOrderDetail(OrderDetail.builder()
						.setOrderId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())))
						.setProducts(restaurantApprovalRequest.getProducts().stream().map(p -> {
							return Product.builder()
									.setProductId(p.getId())
									.setQuantity(p.getQuantity())
									.build();
						}).collect(Collectors.toList()))
						.setTotalAmount(new Money(restaurantApprovalRequest.getPrice()))
						.setOrderStatus(
								OrderStatus.valueOf(restaurantApprovalRequest.getRestaurantOrderStatus().name()))
						.build())
				.build();
	}

	public OrderEventPayload orderApprovalEventToOrderEventPayload(OrderApprovalEvent event) {
		return OrderEventPayload.builder()
				.orderId(event.getOrderApproval().getOrderId().getValue().toString())
				.restaurantId(event.getOrderApproval().getRestaurantId().getValue().toString())
				.createdAt(event.getCreatedAt())
				.orderApprovalStatus(event.getOrderApproval().getOrderApprovalStatus().name())
				.failureMessages(event.getFailureMessages())
				.build();
	}

}
