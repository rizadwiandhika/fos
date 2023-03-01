package com.food.ordering.system.customer.service.dataaccess.outbox.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.food.ordering.system.outbox.OutboxStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "customer_outbox", schema = "customer")
public class CustomerOutboxEntity {

	@Id
	private UUID id;
	private String payload;
	@Enumerated(value = EnumType.STRING)
	private OutboxStatus outboxStatus;
	@Version
	private int version;
	private ZonedDateTime createdAt;
	private ZonedDateTime processedAt;

}
