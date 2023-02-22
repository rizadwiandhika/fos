package com.food.ordering.system.order.service.dataaccess.customer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.ordering.system.order.service.dataaccess.customer.entity.CustomerEntity;

@Repository // Spring will create JPA repository proxy class & will delegate all method
						// calls to this proxy class to complete the database operations
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

}
