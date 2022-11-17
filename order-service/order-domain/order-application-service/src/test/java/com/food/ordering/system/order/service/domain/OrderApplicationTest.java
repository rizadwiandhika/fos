package com.food.ordering.system.order.service.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationTest {

	@Autowired
	private OrderApplicationService orderApplicationService;

	@Autowired
	private OrderDataMapper orderDataMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private CustomerRepository customerRepository;

	private final UUID PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
	private final UUID RESTAURANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
	private final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
	private final BigDecimal PRICE = new BigDecimal("200.00");

	private CreateOrderCommand createOrderCommand;
	private CreateOrderCommand createOrderCommandWrongPrice;
	private CreateOrderCommand createOrderCommandWrongProductPrice;

	@BeforeAll
	public void init() {
		createOrderCommand = CreateOrderCommand.builder()
				.customerId(CUSTOMER_ID)
				.restaurantId(RESTAURANT_ID)
				.price(PRICE)
				.address(OrderAddress.builder()
						.postalCode("12345")
						.street("street 1")
						.city("Paris")
						.build())
				.orderItems(List.of(OrderItem.builder()
						.productId(PRODUCT_ID)
						.quantity(1)
						.price(new BigDecimal("50.00"))
						.subTotal(new BigDecimal("50.00"))
						.build(),
						OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(3)
								.price(new BigDecimal("50.00"))
								.subTotal(new BigDecimal("150.00"))
								.build()))
				.build();

		createOrderCommandWrongPrice = CreateOrderCommand.builder()
				.customerId(CUSTOMER_ID)
				.restaurantId(RESTAURANT_ID)
				.price(new BigDecimal("250.00"))
				.address(OrderAddress.builder()
						.postalCode("12345")
						.street("street 1")
						.city("Paris")
						.build())
				.orderItems(List.of(OrderItem.builder()
						.productId(PRODUCT_ID)
						.quantity(1)
						.price(new BigDecimal("50.00"))
						.subTotal(new BigDecimal("50.00"))
						.build(),
						OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(3)
								.price(new BigDecimal("50.00"))
								.subTotal(new BigDecimal("150.00"))
								.build()))
				.build();

		createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
				.customerId(CUSTOMER_ID)
				.restaurantId(RESTAURANT_ID)
				.price(new BigDecimal("210.00"))
				.address(OrderAddress.builder()
						.postalCode("12345")
						.street("street 1")
						.city("Paris")
						.build())
				.orderItems(List.of(OrderItem.builder()
						.productId(PRODUCT_ID)
						.quantity(1)
						.price(new BigDecimal("60.00"))
						.subTotal(new BigDecimal("60.00"))
						.build(),
						OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(3)
								.price(new BigDecimal("50.00"))
								.subTotal(new BigDecimal("150.00"))
								.build()))
				.build();

		Customer customer = new Customer();
		customer.setId(new CustomerId(CUSTOMER_ID));

		Restaurant restaurant = Restaurant.builder()
				.withRestaurantId(new RestaurantId(RESTAURANT_ID))
				.withProducts(List.of(
						new Product(new ProductId(PRODUCT_ID), "product 1", new Money(new BigDecimal("50.00"))),
						new Product(new ProductId(PRODUCT_ID), "product 2", new Money(new BigDecimal("50.00")))))
				.withActive(true)
				.build();

		Order order = orderDataMapper.createOrderToOrder(createOrderCommand);
		order.setId(new OrderId(ORDER_ID));

		when(customerRepository.findCustomer(createOrderCommand.getCustomerId())).thenReturn(Optional.of(customer));
		when(restaurantRepository
				.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
				.thenReturn(Optional.of(restaurant));
		when(orderRepository.save(any(Order.class))).thenReturn(order);
	}

	@Test
	public void testCreateOrder() {
		CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);

		assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
		assertNotNull(createOrderResponse.getOrderTrackingId());
	}

	@Test
	public void testCreateOrderWrongPrice() {
		OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
				() -> orderApplicationService.createOrder(createOrderCommandWrongPrice));

		assertEquals("Total price: 250.00 is not equal to sum of order items: 200.00", orderDomainException.getMessage());
	}

	@Test
	public void testCreateOrderCommandWrongProductPrice() {
		OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
				() -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));

		assertEquals(
				"Order item price: 60.00 is not valid for product: 00000000-0000-0000-0000-000000000001",
				orderDomainException.getMessage());
	}

	@Test
	public void testCreateOrderWithPassiveRestaurant() {
		Restaurant restaurant = Restaurant.builder()
				.withRestaurantId(new RestaurantId(RESTAURANT_ID))
				.withProducts(List.of(
						new Product(new ProductId(PRODUCT_ID), "product 1", new Money(new BigDecimal("50.00"))),
						new Product(new ProductId(PRODUCT_ID), "product 2", new Money(new BigDecimal("50.00")))))
				.withActive(false)
				.build();

		when(restaurantRepository
				.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
				.thenReturn(Optional.of(restaurant));

		OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
				() -> orderApplicationService.createOrder(createOrderCommand));

		assertEquals(String.format("Restaurant (id: %s) is not active", restaurant.getId().getValue()),
				orderDomainException.getMessage());
	}

}
