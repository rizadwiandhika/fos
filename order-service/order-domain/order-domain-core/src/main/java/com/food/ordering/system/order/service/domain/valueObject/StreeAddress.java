package com.food.ordering.system.order.service.domain.valueObject;

import java.util.UUID;

public class StreeAddress {
	private final UUID id;
	private final String street;
	private final String postalCode;
	private final String city;

	public StreeAddress(UUID id, String street, String postalCode, String city) {
		this.id = id;
		this.street = street;
		this.postalCode = postalCode;
		this.city = city;
	}

	public UUID getId() {
		return id;
	}

	public String getStreet() {
		return street;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCity() {
		return city;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StreeAddress other = (StreeAddress) obj;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (postalCode == null) {
			if (other.postalCode != null)
				return false;
		} else if (!postalCode.equals(other.postalCode))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		return true;
	}

}
