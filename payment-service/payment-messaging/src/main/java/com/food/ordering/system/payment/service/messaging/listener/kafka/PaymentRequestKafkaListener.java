package com.food.ordering.system.payment.service.messaging.listener.kafka;

import java.sql.SQLException;
import java.util.List;

import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentRequestKafkaListener implements KafkaConsumer<PaymentRequestAvroModel> {

	private final PaymentMessagingDataMapper paymentMessagingDataMapper;
	private final PaymentRequestMessageListener paymentRequestMessageListener;

	public PaymentRequestKafkaListener(PaymentMessagingDataMapper paymentMessagingDataMapper,
			PaymentRequestMessageListener paymentRequestMessageListener) {
		this.paymentMessagingDataMapper = paymentMessagingDataMapper;
		this.paymentRequestMessageListener = paymentRequestMessageListener;
	}

	// Wheen there is an exception thrown, this method will retry to read from kafka
	// to process it again
	@Override
	@KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}", topics = "${payment-service.payment-request-topic-name}")
	public void recieve(@Payload List<PaymentRequestAvroModel> messages,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

		log.info("{} number of payment request recieved with keys: {}, partitions: {}, and offsets: {}",
				messages.size(),
				keys.toString(),
				partitions.toString(),
				offsets.toString());

		messages.forEach(message -> {
			try {
				if (PaymentOrderStatus.CANCELLED == message.getPaymentOrderStatus()) {
					paymentRequestMessageListener
							.cancelPayment(paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(message));
				} else if (PaymentOrderStatus.PENDING == message.getPaymentOrderStatus()) {
					paymentRequestMessageListener
							.completePayment(
									paymentMessagingDataMapper.paymentRequestAvroModelToPaymentRequest(message));
				}
			} catch (DataAccessException e) {
				SQLException sqlException = (SQLException) e.getRootCause();
				if (sqlException != null && sqlException.getSQLState() != null
						&& PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
					log.error(
							"Caught UNIQUE_VIOLATION error with SQL state: {}, in PaymentRequestKafkaListener for order id: {}",
							sqlException.getSQLState(), message.getOrderId());
					return;
				}

				// Throw to make this recieve method re-read again the message in kafka
				throw new PaymentApplicationServiceException(
						"Throwing DataAccessException in PaymentRequestKafkaListener. Message: " + e.getMessage(), e);

			} catch (PaymentNotFoundException e) {
				log.error("No payment found for order id: {}", message.getOrderId());
			}
		});

	}

}
