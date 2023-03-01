package com.food.ordering.system.customer.service.messaging.mapper;

import org.springframework.stereotype.Component;

import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerEventPayload;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;

@Component
public class CustomerMessagingDataMapper {

	public CustomerAvroModel customerEventPayloadToCustomerAvroModel(CustomerEventPayload payload) {
		return CustomerAvroModel.newBuilder()
				.setId(payload.getId().toString())
				.setUsername(payload.getUsername())
				.setFirstName(payload.getFirstname())
				.setLastName(payload.getLastName())
				.build();
	}

}
