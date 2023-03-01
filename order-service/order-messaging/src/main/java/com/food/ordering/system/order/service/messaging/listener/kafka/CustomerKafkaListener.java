package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.customer.CustomerMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomerKafkaListener implements KafkaConsumer<CustomerAvroModel> {

	private final CustomerMessageListener listener;
	private final OrderMessagingDataMapper mapper;

	public CustomerKafkaListener(CustomerMessageListener listener, OrderMessagingDataMapper mapper) {
		this.listener = listener;
		this.mapper = mapper;
	}

	@Override
	@KafkaListener(id = "${kafka-consumer-config.customer-group-id}", topics = "${order-service.customer-topic-name}")
	public void recieve(List<CustomerAvroModel> messages, List<String> keys, List<Integer> partitions,
			List<Long> offsets) {

		messages.forEach((message) -> {
			listener.createCustomer(mapper.customerAvroModelToCustomerMessage(message));
			log.info("Finished processing create customer for: {}", message.getUsername());
		});

	}

}
