package com.food.ordering.system.kafka.producer.service.impl;

import java.io.Serializable;

import javax.annotation.PreDestroy;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {

	private final KafkaTemplate<K, V> kafkaTemplate;

	public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void send(String topicName, K key, V message, ListenableFutureCallback<SendResult<K, V>> callback) {
		log.info("Sending message={} to topics={}", message, topicName);

		try {
			ListenableFuture<SendResult<K, V>> kafkaFutureResult = kafkaTemplate.send(topicName, key, message);
			kafkaFutureResult.addCallback(callback);
		} catch (KafkaException e) {
			log.error("Error on kafka producer with key: {} message: {} and exception: {}", key, message, e.getMessage());
			throw new KafkaProducerException(String.format("Error on kafka producer with key: %s message: %s", key, message));
		}
	}

	@PreDestroy
	public void close() {
		if (kafkaTemplate != null) {
			log.info("Closing kafka producer!");
			kafkaTemplate.destroy();
		}
	}

}
