package com.food.ordering.system.order.service.messaging.publisher.kafka;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {

	private final OrderMessagingDataMapper orderMessagingDataMapper;
	private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
	private final OrderServiceConfigData orderServiceConfigData;
	private final KafkaMessageHelper kafkaMessageHelper;

	public OrderPaymentEventKafkaPublisher(OrderMessagingDataMapper orderMessagingDataMapper,
			KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
			OrderServiceConfigData orderServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
		this.orderMessagingDataMapper = orderMessagingDataMapper;
		this.kafkaProducer = kafkaProducer;
		this.orderServiceConfigData = orderServiceConfigData;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	public void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
			BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {

		OrderPaymentEventPayload eventPayload = kafkaMessageHelper
				.getEventPayload(orderPaymentOutboxMessage.getPayload(), OrderPaymentEventPayload.class);
		String sagaId = orderPaymentOutboxMessage.getSagaId().toString();
		try {

			PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
					.orderPaymentEventPayloadToPaymentRequestAvroModel(sagaId,
							eventPayload);

			kafkaProducer.send(
					orderServiceConfigData.getPaymentRequestTopicName(),
					sagaId,
					paymentRequestAvroModel,
					kafkaMessageHelper.getKafkaCallback(
							orderServiceConfigData.getPaymentRequestTopicName(),
							paymentRequestAvroModel,
							orderPaymentOutboxMessage,
							outboxCallback,
							eventPayload.getOrderId(),
							"PaymentRequestAvroModel"));
			log.info("PaymentRequestAvroModel published for orderId: {} and sagaId: {}", eventPayload.getOrderId(),
					sagaId);

		} catch (Exception e) {
			log.error("Unable to publish PaymentRequestAvroModel for orderId: {} and sagaId: {}",
					eventPayload.getOrderId(), sagaId, e);
		}

	}

}
