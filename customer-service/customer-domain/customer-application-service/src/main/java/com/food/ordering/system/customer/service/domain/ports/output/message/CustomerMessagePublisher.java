package com.food.ordering.system.customer.service.domain.ports.output.message;

import java.util.function.BiConsumer;

import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

public interface CustomerMessagePublisher {

	public void publish(CustomerOutboxMessage customerOutboxMessage,
			BiConsumer<CustomerOutboxMessage, OutboxStatus> callback);

}
