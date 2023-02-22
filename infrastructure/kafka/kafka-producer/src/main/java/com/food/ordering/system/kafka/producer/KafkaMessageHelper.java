package com.food.ordering.system.kafka.producer;

import java.util.function.BiConsumer;

import javax.management.RuntimeErrorException;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaMessageHelper {

	private final ObjectMapper objectMapper;

	public KafkaMessageHelper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
			String responseTopicName, T avroModel, U outboxMessage, BiConsumer<U, OutboxStatus> callback,
			String orderId, String avroModelName) {
		return new ListenableFutureCallback<SendResult<String, T>>() {
			@Override
			public void onFailure(Throwable ex) {
				log.error("Error while sending {} " +
						"message {} and outbox type: {} to topic {}", avroModelName, avroModel.toString(),
						outboxMessage.getClass().getName(), responseTopicName,
						ex);

				callback.accept(outboxMessage, OutboxStatus.FAILED);
			}

			@Override
			public void onSuccess(SendResult<String, T> result) {
				RecordMetadata metadata = result.getRecordMetadata();

				log.info("Received successful response from Kafka for order id: {} " +
						"Topic: {} Partition: {} Offset: {} Timestamp: {}",
						orderId,
						metadata.topic(),
						metadata.partition(),
						metadata.offset(),
						metadata.timestamp());

				callback.accept(outboxMessage, OutboxStatus.COMPLETED);

			}
		};
	}

	public <T> T getEventPayload(String payload, Class<T> outputType) throws RuntimeException {
		try {
			return objectMapper.readValue(payload, outputType);
		} catch (Exception e) {
			log.error("Unable to parse {} for saga id: {}", outputType.getName(), e);
			throw new RuntimeException("asdada", e);
		}
	}
}
