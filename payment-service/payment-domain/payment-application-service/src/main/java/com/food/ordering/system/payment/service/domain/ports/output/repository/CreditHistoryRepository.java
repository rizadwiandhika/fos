package com.food.ordering.system.payment.service.domain.ports.output.repository;

import java.util.List;
import java.util.Optional;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;

public interface CreditHistoryRepository {
	// save method
	// findByCustomerId method

	CreditHistory save(CreditHistory creditHistory);

	Optional<List<CreditHistory>> findByCustomerId(CustomerId customerId);

}
