package com.food.ordering.system.payment.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.payment.service.domain.valueobject.CreditEntryId;

public class CreditEntry extends BaseEntity<CreditEntryId> {

	private final CustomerId customerId;
	private Money totalCreditAmount;

	private CreditEntry(Builder builder) {
		setId(builder.creditEntryId);
		this.customerId = builder.customerId;
		this.totalCreditAmount = builder.totalCreditAmount;
	}

	// Business logic methods
	public void addCredit(Money amount) {
		totalCreditAmount = totalCreditAmount.add(amount);
	}

	public void substractCredit(Money amount) {
		totalCreditAmount = totalCreditAmount.substract(amount);
	}

	// Getter
	public CustomerId getCustomerId() {
		return customerId;
	}

	public Money getTotalCreditAmount() {
		return totalCreditAmount;
	}

	// Builder pattern for this class
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private CreditEntryId creditEntryId;
		private CustomerId customerId;
		private Money totalCreditAmount;

		public Builder creditEntryId(CreditEntryId creditEntryId) {
			this.creditEntryId = creditEntryId;
			return this;
		}

		public Builder customerId(CustomerId customerId) {
			this.customerId = customerId;
			return this;
		}

		public Builder totalCreditAmount(Money totalCreditAmount) {
			this.totalCreditAmount = totalCreditAmount;
			return this;
		}

		public CreditEntry build() {
			return new CreditEntry(this);
		}
	}

}
