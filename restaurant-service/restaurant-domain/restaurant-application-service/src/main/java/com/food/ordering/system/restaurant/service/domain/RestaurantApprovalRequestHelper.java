package com.food.ordering.system.restaurant.service.domain;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RestaurantApprovalRequestHelper {

	private final RestaurantDomainService restaurantDomainService;
	private final RestaurantDataMapper restaurantDataMapper;
	private final RestaurantRepository restaurantRepository;
	private final OrderApprovalRepository orderApprovalRepository;
	private final OrderApprovedMessagePublisher orderApprovedMessagePublisher;
	private final OrderRejectedMessagePublisher orderRejectedMessagePublisher;

	public RestaurantApprovalRequestHelper(RestaurantDomainService restaurantDomainService,
			RestaurantDataMapper restaurantDataMapper, RestaurantRepository restaurantRepository,
			OrderApprovalRepository orderApprovalRepository,
			OrderApprovedMessagePublisher orderApprovedMessagePublisher,
			OrderRejectedMessagePublisher orderRejectedMessagePublisher) {
		this.restaurantDomainService = restaurantDomainService;
		this.restaurantDataMapper = restaurantDataMapper;
		this.restaurantRepository = restaurantRepository;
		this.orderApprovalRepository = orderApprovalRepository;
		this.orderApprovedMessagePublisher = orderApprovedMessagePublisher;
		this.orderRejectedMessagePublisher = orderRejectedMessagePublisher;
	}

	@Transactional
	public OrderApprovalEvent persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
		log.info("Processing restaurant approval for order: {}", restaurantApprovalRequest.getOrderId());
		java.util.List<String> failureMessages = new ArrayList<String>();

		Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
		OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validate(restaurant, failureMessages,
				orderApprovedMessagePublisher,
				orderRejectedMessagePublisher);

		orderApprovalRepository.save(orderApprovalEvent.getOrderApproval());
		return orderApprovalEvent;
	}

	private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
		Restaurant restaurant = restaurantDataMapper
				.restaurantApprovalRequestToRestaurant(restaurantApprovalRequest);

		Optional<Restaurant> res = restaurantRepository.findRestaurantInformation(restaurant);

		if (res.isEmpty()) {
			log.error("Restaurant: {} not found for order: {}", restaurantApprovalRequest.getRestaurantId(),
					restaurantApprovalRequest.getOrderId());
			throw new RestaurantNotFoundException("Restaurant: " + restaurantApprovalRequest.getRestaurantId()
					+ " not found for order: " + restaurantApprovalRequest.getOrderId());
		}

		Restaurant restaurantEntity = res.get();
		restaurant.setActive(restaurantEntity.isActive());
		restaurant.getOrderDetail().getProducts().forEach(product -> {
			restaurantEntity.getOrderDetail().getProducts().forEach(p -> {
				if (p.getId().equals(product.getId())) {
					product.updateWithConfirmedNamePriceAndAvailability(p.getName(), p.getPrice(), p.isAvailable());
				}
			});
		});

		restaurant.getOrderDetail().setId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())));
		return restaurant;
	}

}
