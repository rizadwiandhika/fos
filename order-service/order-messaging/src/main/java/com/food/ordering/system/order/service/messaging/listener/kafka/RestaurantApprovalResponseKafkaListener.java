package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {

	private static final CharSequence FAILURE_MESSAGE_DELIMITER = ",";
	private final RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener;
	private final OrderMessagingDataMapper orderMessagingDataMapper;

	public RestaurantApprovalResponseKafkaListener(
			RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener,
			OrderMessagingDataMapper orderMessagingDataMapper) {
		this.restaurantApprovalResponseMessageListener = restaurantApprovalResponseMessageListener;
		this.orderMessagingDataMapper = orderMessagingDataMapper;
	}

	// * When this method throws an error, Spring will read again the sam message
	// * from the Kafka. So it does a retry for a failed message, which is great
	@Override
	@KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}", topics = "${order-service.restaurant-approval-response-topic-name}")
	public void recieve(@Payload List<RestaurantApprovalResponseAvroModel> messages,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

		log.info(
				"[RestaurantApprovalResponseKafkaListener] {} number of payment response recieved with keys: partitions: {} and offsets: {}",
				messages.size(),
				keys.toString(),
				partitions.toString(),
				offsets.toString());

		messages.forEach(message -> {
			try {
				if (OrderApprovalStatus.APPROVED == message.getOrderApprovalStatus()) {
					log.info("Processing approved order for order id: {}", message.getOrderId());
					restaurantApprovalResponseMessageListener.orderApproved(
							orderMessagingDataMapper.approvalResponseAvroModelToRestaurantApprovalResponse(message));
				} else if (OrderApprovalStatus.REJECTED == message.getOrderApprovalStatus()) {
					log.info("Processing rejected order for order id: {} with failures: {}", message.getOrderId(),
							String.join(FAILURE_MESSAGE_DELIMITER, message.getFailureMessages()));

					restaurantApprovalResponseMessageListener.orderRejected(
							orderMessagingDataMapper.approvalResponseAvroModelToRestaurantApprovalResponse(message));
				}
			} catch (OptimisticLockingFailureException e) {
				// Caught the error and do nothing, so that it wont retry
				log.error("OptimisticLockingFailureException error caught for order id: {}", message.getOrderId());
			} catch (OrderNotFoundException e) {
				// Caught the error and do nothing, so that it wont retry
				log.error("OrderNotFoundException error caught for order id: {}", message.getOrderId());
			}
		});
	}

}
