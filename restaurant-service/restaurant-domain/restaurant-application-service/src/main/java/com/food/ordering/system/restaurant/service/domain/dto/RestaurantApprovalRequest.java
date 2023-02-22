package com.food.ordering.system.restaurant.service.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.food.ordering.system.domain.valueObject.RestaurantOrderStatus;
import com.food.ordering.system.restaurant.service.domain.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantApprovalRequest {
	private String id;
	private String sagaId;
	private String restaurantId;
	private String orderId;
	private RestaurantOrderStatus restaurantOrderStatus;
	private List<Product> products;
	private BigDecimal price;
	private Instant createdAt;
}
