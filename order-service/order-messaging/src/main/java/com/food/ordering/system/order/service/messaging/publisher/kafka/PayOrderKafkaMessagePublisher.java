package com.food.ordering.system.order.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PayOrderKafkaMessagePublisher implements OrderPaidRestaurantRequestMessagePublisher {

	private final OrderMessagingDataMapper orderMessagingDataMapper;
	private final OrderServiceConfigData orderServiceConfigData;
	private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
	private final KafkaMessageHelper kafkaMessageHelper;

	public PayOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
			OrderServiceConfigData orderServiceConfigData,
			KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
			KafkaMessageHelper orderKafkaMessageHelper) {
		this.orderMessagingDataMapper = orderMessagingDataMapper;
		this.orderServiceConfigData = orderServiceConfigData;
		this.kafkaProducer = kafkaProducer;
		this.kafkaMessageHelper = orderKafkaMessageHelper;
	}

	public void publish(OrderPaidEvent domainEvent) {
		String orderId = domainEvent.getOrder().getId().getValue().toString();

		try {
			RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper
					.orderPaidEventToRestaurantApprovalRequestAvroModel(domainEvent);

			kafkaProducer.send(orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
					orderId,
					restaurantApprovalRequestAvroModel,
					kafkaMessageHelper.getKafkaCallback(
							orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
							restaurantApprovalRequestAvroModel,
							orderId,
							"RestaurantApprovalRequestAvroModel"));

			log.info("RestaurantApprovalRequestAvroModel sent to Kafka for order id: {}", orderId);
		} catch (Exception e) {
			// 2023-02-14 15:00:12.125 ERROR 33578 --- [-consumer-0-C-1]
			// .o.s.m.p.k.PayOrderKafkaMessagePublisher :
			// Error while sending RestaurantApprovalRequestAvroModel mesage To Kafka with
			// order id: 7e8508a5-1c4b-4cbd-8a6c-4df50514b0d8,
			// error: Field restaurantId type:STRING pos:2 not set and has no default value
			log.error("Error while sending RestaurantApprovalRequestAvroModel mesage " +
					"To Kafka with order id: {}, error: {}", orderId, e.getMessage());
		}

	}

}
