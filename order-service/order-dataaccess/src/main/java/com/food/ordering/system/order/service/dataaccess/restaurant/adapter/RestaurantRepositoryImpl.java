package com.food.ordering.system.order.service.dataaccess.restaurant.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.order.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.dataaccess.restaurant.repository.RestaurantJPARepository;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

	private final RestaurantJPARepository restaurantJPARepository;
	private final RestaurantDataAccessMapper restaurantDataAccessMapper;

	public RestaurantRepositoryImpl(RestaurantJPARepository restaurantJPARepository,
			RestaurantDataAccessMapper restaurantDataAccessMapper) {
		this.restaurantJPARepository = restaurantJPARepository;
		this.restaurantDataAccessMapper = restaurantDataAccessMapper;
	}

	@Override
	public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
		List<UUID> restaurantProducts = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
		Optional<List<RestaurantEntity>> restaurantEntities = restaurantJPARepository
				.findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), restaurantProducts);

		return restaurantEntities.map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
		// restaurantEntities.map()
	}

}
