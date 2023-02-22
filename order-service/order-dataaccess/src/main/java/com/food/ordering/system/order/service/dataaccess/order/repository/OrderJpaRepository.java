package com.food.ordering.system.order.service.dataaccess.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;

@Repository // Spring will create JPA repository proxy class & will delegate all method
						// calls to this proxy class to complete the database operations
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

	Optional<OrderEntity> findByTrackingId(UUID trackingId);

}
