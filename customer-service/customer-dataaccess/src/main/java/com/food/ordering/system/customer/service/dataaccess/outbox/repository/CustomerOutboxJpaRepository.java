package com.food.ordering.system.customer.service.dataaccess.outbox.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.ordering.system.customer.service.dataaccess.outbox.entity.CustomerOutboxEntity;
import com.food.ordering.system.outbox.OutboxStatus;

@Repository
public interface CustomerOutboxJpaRepository extends JpaRepository<CustomerOutboxEntity, UUID> {

	Optional<List<CustomerOutboxEntity>> findByOutboxStatus(OutboxStatus status);

}
