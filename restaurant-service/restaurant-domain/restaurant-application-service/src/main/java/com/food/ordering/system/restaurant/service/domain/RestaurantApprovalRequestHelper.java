package com.food.ordering.system.restaurant.service.domain;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
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
	private final OrderOutboxHelper orderOutboxHelper;
	private final RestaurantApprovalResponseMessagePublisher responseMessagePublisher;

	public RestaurantApprovalRequestHelper(RestaurantDomainService restaurantDomainService,
			RestaurantDataMapper restaurantDataMapper, RestaurantRepository restaurantRepository,
			OrderApprovalRepository orderApprovalRepository, OrderOutboxHelper orderOutboxHelper,
			RestaurantApprovalResponseMessagePublisher responseMessagePublisher) {
		this.restaurantDomainService = restaurantDomainService;
		this.restaurantDataMapper = restaurantDataMapper;
		this.restaurantRepository = restaurantRepository;
		this.orderApprovalRepository = orderApprovalRepository;
		this.orderOutboxHelper = orderOutboxHelper;
		this.responseMessagePublisher = responseMessagePublisher;
	}

	@Transactional
	public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
		if (publishIfPreviousApprovalAlreadyProcessed(restaurantApprovalRequest)) {
			log.info("Approval outbox has been processed previously. Republish the event");
			return;
		}

		log.info("Processing restaurant approval for order: {}", restaurantApprovalRequest.getOrderId());
		java.util.List<String> failureMessages = new ArrayList<String>();

		Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
		OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validate(restaurant, failureMessages);

		orderApprovalRepository.save(orderApprovalEvent.getOrderApproval());
		orderOutboxHelper.saveOrderOutboxMessage(
				restaurantDataMapper.orderApprovalEventToOrderEventPayload(orderApprovalEvent),
				restaurantApprovalRequest.getSagaId(),
				OutboxStatus.STARTED);
	}

	private boolean publishIfPreviousApprovalAlreadyProcessed(RestaurantApprovalRequest restaurantApprovalRequest) {
		Optional<OrderOutboxMessage> op = orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndOutboxStatus(
				UUID.fromString(restaurantApprovalRequest.getSagaId()), OutboxStatus.COMPLETED);

		if (op.isPresent()) {
			responseMessagePublisher.publish(op.get(), orderOutboxHelper::updateOutboxStatus);
			return true;
		}

		return false;
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
