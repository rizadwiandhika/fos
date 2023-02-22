package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentCancelledKafkaMessagePublisher implements PaymentCancelledMessagePublisher {

	private final PaymentMessagingDataMapper paymentMessagingDataMapper;
	private final KafkaProducer<String, PaymentResponseAvroModel> producer;
	private final PaymentServiceConfigData paymentServiceConfigData;
	private final KafkaMessageHelper kafkaMessageHelper;

	public PaymentCancelledKafkaMessagePublisher(PaymentMessagingDataMapper paymentMessagingDataMapper,
			KafkaProducer<String, PaymentResponseAvroModel> producer, PaymentServiceConfigData paymentServiceConfigData,
			KafkaMessageHelper kafkaMessageHelper) {
		this.paymentMessagingDataMapper = paymentMessagingDataMapper;
		this.producer = producer;
		this.paymentServiceConfigData = paymentServiceConfigData;
		this.kafkaMessageHelper = kafkaMessageHelper;
	}

	@Override
	public void publish(PaymentCancelledEvent domainEvent) {
		String orderId = domainEvent.getPayment().getOrderId().getValue().toString();

		log.info("Recieved PaymentCancelledEvent for order id: {}", orderId);

		PaymentResponseAvroModel paymentResponseAvroModel = paymentMessagingDataMapper
				.paymentCancelledEventToPaymentResponseAvroModel(domainEvent);

		try {
			producer.send(paymentServiceConfigData.getPaymentResponseTopicName(), orderId, paymentResponseAvroModel,
					kafkaMessageHelper.getKafkaCallback(
							paymentServiceConfigData.getPaymentResponseTopicName(), paymentResponseAvroModel, orderId,
							"PaymentResponseAvroModel"));

			log.info("PaymentResponseAvroModel sent to kafka for order id: {}", orderId);
		} catch (Exception e) {
			log.error("Error while sending PaymentResponseAvroModel message " +
					" to kafka with order id: {}, error: {}", orderId, e.getMessage());
		}

	}

}
