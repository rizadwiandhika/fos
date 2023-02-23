package com.food.ordering.system.customer.service.dataaccess.customer.entity;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "customers", schema = "customer")
public class CustomerEntity {

	@Id
	private UUID id;
	private String username;
	private String firstName;
	private String lastName;

}
