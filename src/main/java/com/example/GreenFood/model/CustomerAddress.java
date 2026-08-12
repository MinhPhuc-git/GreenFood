package com.example.GreenFood.model;

import jakarta.persistence.*;

@Entity
@Table (name = "customeraddress")
public class CustomerAddress {
	@EmbeddedId
	private CustomerAddressId id;
	
    @ManyToOne
    @MapsId("customerId")
    @JoinColumn(name = "customer_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Customer customer;

    @ManyToOne
    @MapsId("addressId")
    @JoinColumn(name = "address_id")
    private Address address;
    
    @Column(name = "is_default")
    private Boolean isDefault;

	public CustomerAddress() {
	}

	public CustomerAddress(CustomerAddressId id, Customer customer, Address address, Boolean isDefault) {
		this.id = id;
		this.customer = customer;
		this.address = address;
		this.isDefault = isDefault;
	}

	public CustomerAddressId getId() { return id; }
	public void setId(CustomerAddressId id) { this.id = id; }
	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }
	public Address getAddress() { return address; }
	public void setAddress(Address address) { this.address = address; }
	public Boolean getIsDefault() { return isDefault; }
	public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
