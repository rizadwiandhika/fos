package com.food.ordering.sysstem.restaurant.service.domain.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.domain.valueObject.RestaurantOrderStatus;
import com.food.ordering.system.restaurant.service.domain.RestaurantDomainService;
import com.food.ordering.system.restaurant.service.domain.RestaurantServiceApplication;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalMessageRequestListener;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;
import com.google.common.reflect.TypeToken;

@SpringBootTest(classes = RestaurantServiceApplication.class)
public class RestaurantServiceApplicationTest {

	@Autowired
	private RestaurantApprovalMessageRequestListener approvalListener;

	@Mock
	private RestaurantDomainService restaurantDomainService;

	private static final String APPROVAL_ID = "8070651f-dd61-4c3b-9a4d-f7a48c95c1f1";
	private static final String SAGA_ID = "8070651f-dd61-4c3b-9a4d-f7a48c95c1f2";
	private static final String RESTAURANT_ID = "8070651f-dd61-4c3b-9a4d-f7a48c95c1f3";
	private static final String ORDER_ID = "8070651f-dd61-4c3b-9a4d-f7a48c95c1f4";

	@Test
	void testDoubleApproval() {
		// approvalListener.approveOrder(getApprovalRequest());
		when(restaurantDomainService.validate(any(Restaurant.class), anyList())).thenReturn(getOrderApprovalEvent());

		OrderApprovalEvent event = restaurantDomainService.validate(Restaurant.builder().build(), List.of());

		verify(restaurantDomainService).validate(Restaurant.builder().build(), List.of());
		assertEquals(APPROVAL_ID, event.getOrderApproval().getId().getValue().toString());

	}

	private OrderApprovalEvent getOrderApprovalEvent() {
		OrderApproval approval = OrderApproval.builder()
				.setOrderApprovalId(new OrderApprovalId(UUID.fromString(APPROVAL_ID)))
				.setRestaurantId(new RestaurantId(UUID.fromString(RESTAURANT_ID)))
				.setOrderId(new OrderId(UUID.fromString(ORDER_ID)))
				.setOrderApprovalStatus(OrderApprovalStatus.APPROVED)
				.build();

		return new OrderApprovedEvent(approval, new RestaurantId(UUID.fromString(RESTAURANT_ID)), List.of(""),
				ZonedDateTime.now());
	}

	private RestaurantApprovalRequest getApprovalRequest() {
		return RestaurantApprovalRequest.builder()
				.id(APPROVAL_ID)
				.sagaId(SAGA_ID)
				.restaurantId(RESTAURANT_ID)
				.orderId(ORDER_ID)
				.restaurantOrderStatus(RestaurantOrderStatus.PAID)
				.build();
	}

}
