package com.food.ordering.system.order.service.dataaccess.restaurant.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;

@Component
public class RestaurantDataAccessMapper {

	public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
		return restaurant.getProducts()
				.stream()
				.map((product) -> product.getId().getValue())
				.collect(Collectors.toList());
	}

	public Restaurant restaurantEntityToRestaurant(List<RestaurantEntity> restaurantEntities) {
		RestaurantEntity restaurantEntity = restaurantEntities.stream().findFirst()
				.orElseThrow(() -> new RestaurantDataAccessException("Restaurant cannot be found!"));

		List<Product> restauraProducts = restaurantEntities.stream()
				.map((restaurant) -> new Product(new ProductId(restaurant.getProductId()), restaurant.getProductName(),
						new Money(restaurant.getProductPrice())))
				.collect(Collectors.toList());

		return Restaurant.builder()
				.withRestaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
				.withProducts(restauraProducts)
				.withActive(restaurantEntity.getRestaurantActive())
				.build();
	}
}
