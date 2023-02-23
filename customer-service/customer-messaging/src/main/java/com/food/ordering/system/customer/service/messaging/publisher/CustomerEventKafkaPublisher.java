package com.food.ordering.system.customer.service.messaging.publisher;

import java.util.function.BiConsumer;

import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.food.ordering.system.customer.service.domain.config.CustomerServiceConfig;
import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerEventPayload;
import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.ports.output.message.CustomerMessagePublisher;
import com.food.ordering.system.customer.service.messaging.mapper.CustomerMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomerEventKafkaPublisher implements CustomerMessagePublisher {

	private final CustomerServiceConfig customerServiceConfig;
	private final KafkaProducer<String, CustomerAvroModel> producer;
	private final CustomerMessagingDataMapper mapper;
	private final KafkaMessageHelper kafkaMessageHelper;

	public CustomerEventKafkaPublisher(CustomerServiceConfig customerServiceConfig,
			KafkaProducer<String, CustomerAvroModel> producer, CustomerMessagingDataMapper mapper,
			KafkaMessageHelper kafkaMessageHelper) {
		this.customerServiceConfig = customerServiceConfig;
		this.producer = producer;
		this.mapper = mapper;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	public void publish(CustomerOutboxMessage customerOutboxMessage,
			BiConsumer<CustomerOutboxMessage, OutboxStatus> callback) {

		try {
			String topicName = customerServiceConfig.getCustomerTopicName();
			CustomerEventPayload eventPayload = kafkaMessageHelper.getEventPayload(customerOutboxMessage.getPayload(),
					CustomerEventPayload.class);
			CustomerAvroModel avroMessage = mapper.customerEventPayloadToCustomerAvroModel(eventPayload);
			String id = eventPayload.getUsername();
			producer.send(
					topicName,
					id,
					avroMessage,
					kafkaMessageHelper.getKafkaCallback(topicName, avroMessage, customerOutboxMessage, callback, id,
							"CustomerAvroModel"));

			log.info("CustomerAvroModel sent to kafka {} topic", topicName);
		} catch (Exception e) {

			log.error("Unable to send CustomerAvroModel to kafka topic", e);
			log.error(e.getMessage());
		}
	}

}
