package com.food.ordering.system.payment.service.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import com.food.ordering.system.domain.valueObject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.dataaccess.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.dataaccess.outbox.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.saga.order.SagaConstant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = { PaymentServiceApplication.class })
public class PaymentRequestMessageListenerTest {

	@Autowired
	private PaymentRequestMessageListener paymentRequestMessageListener;

	@Autowired
	private OrderOutboxJpaRepository orderOutboxJpaRepository;

	private final static String CUSTOMER_ID = "d215b5f8-0249-4dc5-89a3-51fd148cfb41";
	private final static BigDecimal PRICE = new BigDecimal("100");

	@Test
	void testDoublePayment() {
		String sagaId = UUID.randomUUID().toString();

		paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
		try {
			paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
		} catch (DataAccessException e) {
			log.error("DataAccessException occured with sql state: {}",
					((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState(), e);
		}

		assertOrderOutbox(sagaId);
	}

	@Test
	void testDoublePaymentWithThreads() {
		String sagaId = UUID.randomUUID().toString();

		ExecutorService executor = null;

		executor = Executors.newFixedThreadPool(2);
		List<Callable<Object>> tasks = new ArrayList<>();

		tasks.add(Executors.callable(() -> {
			try {
				paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
			} catch (DataAccessException e) {
				log.error("DataAccessException occured in thread 1 with sql state: {}",
						((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState(), e);
			}
		}));

		tasks.add(Executors.callable(() -> {
			try {
				paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
			} catch (DataAccessException e) {
				log.error("DataAccessException occured in thread 2 with sql state: {}",
						((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState(), e);
			}
		}));

		try {
			executor.invokeAll(tasks);
			assertOrderOutbox(sagaId);
		} catch (InterruptedException e) {
			log.error("Error calling complete payment!", e);
		}

		if (executor != null) {
			executor.shutdown();
		}

		// paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
		// try {
		// paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
		// } catch (DataAccessException e) {
		// log.error("DataAccessException occured with sql state: {}",
		// ((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState(), e);
		// }

		assertOrderOutbox(sagaId);
	}

	private void assertOrderOutbox(String sagaId) {
		Optional<OrderOutboxEntity> orderOutboxEntityOp = orderOutboxJpaRepository
				.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(SagaConstant.ORDER_SAGA_NAME,
						UUID.fromString(sagaId), PaymentStatus.COMPLETED, OutboxStatus.STARTED);

		assertTrue(orderOutboxEntityOp.isPresent());
		assertEquals(sagaId, orderOutboxEntityOp.get().getSagaId().toString());
	}

	private PaymentRequest getPaymentRequest(String sagaId) {
		return PaymentRequest.builder()
				.id(UUID.randomUUID().toString())
				.sagaId(sagaId)
				.orderId(UUID.randomUUID().toString())
				.paymentOrderStatus(PaymentOrderStatus.PENDING)
				.customerId(CUSTOMER_ID)
				.price(PRICE)
				.createdAt(Instant.now())
				.build();
	}
}
