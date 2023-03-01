package com.food.ordering.system.order.service.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.persistence.OptimisticLockException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstant;

import lombok.extern.slf4j.Slf4j;

// Run the OrderServiceApplication in the testing context
@SpringBootTest(classes = OrderServiceApplication.class)
@Sql(value = { "classpath:sql/OrderPaymentSagaTestSetup.sql" }) // default executionPhase is BEFORE_TEST_METHOD
@Sql(value = { "classpath:sql/OrderPaymentSagaTestCleanup.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Slf4j
public class OrderPaymentSagaTest {

	@Autowired
	private OrderPaymentSaga orderPaymentSaga;
	@Autowired
	private PaymentOutboxJpaRepository paymentOutboxJpaRepository;

	private final UUID SAGA_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8ca7afa");
	private final UUID ORDER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb17");
	private final UUID CUSTOMER_ID = UUID.fromString("d215b5f8-8249-4dc5-89a3-51fd148cfb41");
	private final UUID PAYMENT_ID = UUID.randomUUID();
	private final BigDecimal PRICE = new BigDecimal("100");

	@Test
	void testDoublePayment() {
		orderPaymentSaga.process(getOrderPaymentResponse());
		orderPaymentSaga.process(getOrderPaymentResponse());
	}

	@Test
	void testDoublePaymentWithThreads() throws InterruptedException {
		Thread thread1 = new Thread(() -> orderPaymentSaga.process(getOrderPaymentResponse()));
		Thread thread2 = new Thread(() -> orderPaymentSaga.process(getOrderPaymentResponse()));

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		assertPaymentOutbox();
	}

	@Test
	void testDoublePaymentWithLatch() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(2);

		Thread thread1 = new Thread(() -> {
			try {
				orderPaymentSaga.process(getOrderPaymentResponse());
			} catch (OptimisticLockException e) {
				log.info("OptimisticLockException in thread 1");
			} finally {
				latch.countDown();
			}
		});
		Thread thread2 = new Thread(() -> {
			try {
				orderPaymentSaga.process(getOrderPaymentResponse());
			} catch (OptimisticLockException e) {
				log.info("OptimisticLockException in thread 2");
			} finally {
				latch.countDown();
			}
		});

		thread1.start();
		thread2.start();

		latch.await();

		assertPaymentOutbox();
	}

	private void assertPaymentOutbox() {
		Optional<PaymentOutboxEntity> result = paymentOutboxJpaRepository.findByTypeAndSagaIdAndSagaStatusIn(
				SagaConstant.ORDER_SAGA_NAME, SAGA_ID,
				List.of(SagaStatus.PROCESSING));

		assertTrue(result.isPresent());
	}

	private PaymentResponse getOrderPaymentResponse() {
		return PaymentResponse.builder()
				.id(UUID.randomUUID().toString())
				.sagaId(SAGA_ID.toString())
				.paymentStatus(PaymentStatus.COMPLETED)
				.orderId(ORDER_ID.toString())
				.customerId(CUSTOMER_ID.toString())
				.price(PRICE)
				.createdAt(Instant.now())
				.failureMessages(new ArrayList<>())
				.build();

	}

}
