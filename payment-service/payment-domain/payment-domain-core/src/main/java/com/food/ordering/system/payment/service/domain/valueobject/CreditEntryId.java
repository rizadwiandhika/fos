package com.food.ordering.system.payment.service.domain.valueobject;

import java.util.UUID;

import com.food.ordering.system.domain.valueObject.BaseId;

public class CreditEntryId extends BaseId<UUID> {

	public CreditEntryId(UUID value) {
		super(value);
	}

}