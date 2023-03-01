package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {

	private final PaymentResponseMessageListener paymentResponseMessageListener;
	private final OrderMessagingDataMapper orderMessagingDataMapper;

	public PaymentResponseKafkaListener(PaymentResponseMessageListener paymentResponseMessageListener,
			OrderMessagingDataMapper orderMessagingDataMapper) {
		this.paymentResponseMessageListener = paymentResponseMessageListener;
		this.orderMessagingDataMapper = orderMessagingDataMapper;
	}

	// * When this method throws an error, Spring will read again the sam message
	// * from the Kafka. So it does a retry for a failed message, which is great
	@Override
	@KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}", topics = "${order-service.payment-response-topic-name}")
	public void recieve(@Payload List<PaymentResponseAvroModel> messages,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

		log.info(
				"[PaymentResponseKafkaListener] {} number of payment response recieved with keys: partitions: {} and offsets: {}",
				messages.size(),
				keys.toString(),
				partitions.toString(),
				offsets.toString());

		messages.forEach(message -> {
			try {
				if (com.food.ordering.system.kafka.order.avro.model.PaymentStatus.COMPLETED == message
						.getPaymentStatus()) {
					log.info("Processing successful payment for order id: {}", message.getOrderId());
					paymentResponseMessageListener
							.paymentCompleted(
									orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(message));
				} else if (com.food.ordering.system.kafka.order.avro.model.PaymentStatus.CANCELLED == message
						.getPaymentStatus()
						|| com.food.ordering.system.kafka.order.avro.model.PaymentStatus.FAILED == message
								.getPaymentStatus()) {
					log.info("Processing unsuccessful payment for order id: {}", message.getOrderId());
					paymentResponseMessageListener
							.paymentCancelled(
									orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(message));
				}
			} catch (OptimisticLockingFailureException e) {
				log.error("OptimisticLockingFailureException error caught for order id: {}", message.getOrderId());
			} catch (OrderNotFoundException e) {
				log.error("OrderNotFoundException error caught for order id: {}", message.getOrderId());

			}
		});
	}

}
