package com.food.ordering.system.order.service.domain;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreateHelper {

	private final OrderDomainService orderDomainService;

	private final OrderRepository orderRepository;

	private final CustomerRepository customerRepository;

	private final RestaurantRepository restaurantRepository;

	private final OrderDataMapper orderDataMapper;

	@Autowired
	public OrderCreateHelper(OrderDomainService orderDomainService, OrderRepository orderRepository,
			CustomerRepository customerRepository, RestaurantRepository restaurantRepository,
			OrderDataMapper orderDataMapper) {
		this.orderDomainService = orderDomainService;
		this.orderRepository = orderRepository;
		this.customerRepository = customerRepository;
		this.restaurantRepository = restaurantRepository;
		this.orderDataMapper = orderDataMapper;
	}

	// @Transactional method should be public, otherwise it will not work
	@Transactional
	public OrderCreatedEvent persistOrder(CreateOrderCommand createOrderCommand) {
		checkCustomer(createOrderCommand.getCustomerId());
		Restaurant restaurant = checkRestaurant(createOrderCommand);
		Order order = orderDataMapper.createOrderToOrder(createOrderCommand);
		OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);

		log.info("Saving order...");

		saveOrder(order);

		return orderCreatedEvent;
	}

	private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
		Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);

		Optional<Restaurant> optionalRsestaurant = restaurantRepository.findRestaurantInformation(restaurant);
		if (optionalRsestaurant.isEmpty()) {
			log.warn("Restaurant not found with id: {}", restaurant.getId());
			throw new OrderDomainException("Restaurant not found. id: " + restaurant.getId());
		}

		return optionalRsestaurant.get();
	}

	private void checkCustomer(@NotNull UUID customerId) {
		Optional<Customer> customer = customerRepository.findCustomer(customerId);

		if (customer.isEmpty()) {
			log.warn("Customer not found with id: {}", customerId);
			throw new OrderDomainException("Customer not found. id: " + customerId);
		}

	}

	private Order saveOrder(Order order) {
		Order result = orderRepository.save(order);
		if (result == null) {
			log.warn("Order not saved");
			throw new OrderDomainException("Order not saved");
		}

		log.info("Order saved with id: {}", result.getId());
		return result;
	}

}
