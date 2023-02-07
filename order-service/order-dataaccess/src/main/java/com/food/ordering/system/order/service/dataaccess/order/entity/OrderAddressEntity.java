package com.food.ordering.system.order.service.dataaccess.order.entity;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor // required by Springboot
@AllArgsConstructor // required for builder pattern
@Table(name = "order_address")
@Entity
public class OrderAddressEntity {

	@Id
	private UUID id;

	@OneToOne(cascade = CascadeType.ALL) // when order is deleted, address should be deleted
	@JoinColumn(name = "ORDER_ID") // foreign key, order_address will have a column called ORDER_ID as foreign key
	private OrderEntity order; // matches with the @OneToOne(mappedBy = "order",...) in OrderEntity

	private String street;
	private String postalCode;
	private String city;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		OrderAddressEntity other = (OrderAddressEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
