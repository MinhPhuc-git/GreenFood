package com.example.GreenFood.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class CustomerAddressId implements Serializable {
	private int customerId;
	private int addressId;

	public CustomerAddressId() {
	}

	public CustomerAddressId(int customerId, int addressId) {
		this.customerId = customerId;
		this.addressId = addressId;
	}

	public int getCustomerId() { return customerId; }
	public void setCustomerId(int customerId) { this.customerId = customerId; }
	public int getAddressId() { return addressId; }
	public void setAddressId(int addressId) { this.addressId = addressId; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CustomerAddressId that = (CustomerAddressId) o;
		return customerId == that.customerId && addressId == that.addressId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(customerId, addressId);
	}
}
