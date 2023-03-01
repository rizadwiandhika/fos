package com.food.ordering.system.order.service.messaging.publisher.kafka;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderApprovalEventKafkaPublisher implements RestaurantApprovalRequestMessagePublisher {

	private final OrderMessagingDataMapper orderMessagingDataMapper;
	private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
	private final OrderServiceConfigData orderServiceConfigData;
	private final KafkaMessageHelper kafkaMessageHelper;

	public OrderApprovalEventKafkaPublisher(OrderMessagingDataMapper orderMessagingDataMapper,
			KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
			OrderServiceConfigData orderServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
		this.orderMessagingDataMapper = orderMessagingDataMapper;
		this.kafkaProducer = kafkaProducer;
		this.orderServiceConfigData = orderServiceConfigData;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	public void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
			BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback) {

		String sagaId = orderApprovalOutboxMessage.getSagaId().toString();

		OrderApprovalEventPayload payload = kafkaMessageHelper.getEventPayload(orderApprovalOutboxMessage.getPayload(),
				OrderApprovalEventPayload.class);

		RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper
				.orderApprovalEventPayloadToRestaurantApprovalRequestAvroModel(sagaId, payload);

		try {
			kafkaProducer.send(orderServiceConfigData.getRestaurantApprovalRequestTopicName(), sagaId,
					restaurantApprovalRequestAvroModel, kafkaMessageHelper.getKafkaCallback(
							orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
							restaurantApprovalRequestAvroModel,
							orderApprovalOutboxMessage,
							outboxCallback,
							payload.getOrderId(),
							"RestaurantApprovalRequestAvroModel"));

		} catch (Exception e) {
			log.error("Unable to publish RestaurantApprovalRequestAvroModel for order id: {}, saga id: {}",
					payload.getOrderId(), sagaId, e);
		}
	}

}
