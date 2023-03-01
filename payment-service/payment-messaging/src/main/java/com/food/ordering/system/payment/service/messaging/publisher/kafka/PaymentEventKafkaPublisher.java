package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentEventKafkaPublisher implements PaymentResponseMessagePublisher {

	private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
	private final PaymentServiceConfigData configData;
	private final PaymentMessagingDataMapper mapper;
	private final KafkaMessageHelper kafkaMessageHelper;

	public PaymentEventKafkaPublisher(KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer,
			PaymentServiceConfigData configData, PaymentMessagingDataMapper mapper,
			KafkaMessageHelper kafkaMessageHelper) {
		this.kafkaProducer = kafkaProducer;
		this.configData = configData;
		this.mapper = mapper;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	public void publish(OrderOutboxMessage orderOutboxMessage, BiConsumer<OrderOutboxMessage, OutboxStatus> callback) {
		String sagaId = orderOutboxMessage.getSagaId().toString();
		OrderEventPayload payload = kafkaMessageHelper.getEventPayload(orderOutboxMessage.getPayload(),
				OrderEventPayload.class);
		PaymentResponseAvroModel avroModel = mapper.orderEventPayloadToPaymentResponseAvroModel(sagaId, payload);

		try {
			kafkaProducer.send(configData.getPaymentResponseTopicName(), sagaId, avroModel,
					kafkaMessageHelper.getKafkaCallback(configData.getPaymentResponseTopicName(), avroModel,
							orderOutboxMessage, callback, payload.getOrderId(), "PaymentResponseAvroModel"));

			log.info("Successfully send message to {} for saga id: and order id: {}",
					configData.getPaymentResponseTopicName(), sagaId, orderOutboxMessage.getSagaId().toString(),
					payload.getOrderId());
		} catch (Exception e) {
			log.error("Unable to publish to {} for saga id: {} and order id: {}",
					configData.getPaymentResponseTopicName(),
					sagaId, payload.getOrderId(), e);
		}
	}

}
