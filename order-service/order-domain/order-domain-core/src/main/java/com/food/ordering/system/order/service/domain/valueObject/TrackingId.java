package com.food.ordering.system.order.service.domain.valueObject;

import java.util.UUID;

import com.food.ordering.system.domain.valueObject.BaseId;

public class TrackingId extends BaseId<UUID> {

	public TrackingId(UUID id) {
		super(id);
	}
}
