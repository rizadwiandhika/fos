package com.food.ordering.system.order.service.dataaccess.restaurant.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.ordering.system.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.order.service.dataaccess.restaurant.entity.RestaurantEntityId;

@Repository
public interface RestaurantJPARepository extends JpaRepository<RestaurantEntity, RestaurantEntityId> {

	// findBy...ProductIdIn: notice the In keyword 
	// this will be converted into SQL query with IN keyword
	Optional<List<RestaurantEntity>> findByRestaurantIdAndProductIdIn(UUID restaurantId, List<UUID> productIds);

}
