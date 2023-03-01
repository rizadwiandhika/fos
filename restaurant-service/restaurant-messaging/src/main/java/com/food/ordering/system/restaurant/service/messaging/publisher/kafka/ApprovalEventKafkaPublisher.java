package com.food.ordering.system.restaurant.service.messaging.publisher.kafka;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApprovalEventKafkaPublisher implements RestaurantApprovalResponseMessagePublisher {

	private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> producer;
	private final RestaurantServiceConfigData configData;
	private final KafkaMessageHelper kafakHelper;
	private final RestaurantMessagingDataMapper mapper;

	public ApprovalEventKafkaPublisher(KafkaProducer<String, RestaurantApprovalResponseAvroModel> producer,
			RestaurantServiceConfigData configData, KafkaMessageHelper kafakHelper,
			RestaurantMessagingDataMapper mapper) {
		this.producer = producer;
		this.configData = configData;
		this.kafakHelper = kafakHelper;
		this.mapper = mapper;
	}

	@Override
	public void publish(OrderOutboxMessage orderOutboxMessage, BiConsumer<OrderOutboxMessage, OutboxStatus> callback) {
		String sagaId = orderOutboxMessage.getSagaId().toString();
		OrderEventPayload payload = kafakHelper.getEventPayload(orderOutboxMessage.getPayload(),
				OrderEventPayload.class);

		RestaurantApprovalResponseAvroModel message = mapper
				.orderEventPayloadToRestaurantApprovalResponseAvroModel(sagaId, payload);

		try {
			producer.send(configData.getRestaurantApprovalResponseTopicName(), sagaId, message,
					kafakHelper.getKafkaCallback(
							configData.getRestaurantApprovalResponseTopicName(), message, orderOutboxMessage, callback,
							payload.getOrderId(), "RestaurantApprovalResponseAvroModel"));
			log.info("Successfully send message to {} for saga id: and order id: {}",
					configData.getRestaurantApprovalResponseTopicName(), sagaId,
					orderOutboxMessage.getSagaId().toString(),
					payload.getOrderId());
		} catch (Exception e) {
			log.error("Unable to publish the messahe to {} for saga id: {}",
					configData.getRestaurantApprovalResponseTopicName(), sagaId, e);
		}
	}

}
