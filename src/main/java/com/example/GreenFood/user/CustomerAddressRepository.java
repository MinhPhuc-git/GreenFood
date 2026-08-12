package com.example.GreenFood.user;

import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.CustomerAddress;
import com.example.GreenFood.model.CustomerAddressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, CustomerAddressId> {
    List<CustomerAddress> findByCustomer(Customer customer);
    Optional<CustomerAddress> findByCustomerAndIsDefaultTrue(Customer customer);
    boolean existsByCustomerAndAddress_StreetIgnoreCaseAndAddress_WardIgnoreCaseAndAddress_DistrictIgnoreCaseAndAddress_CityIgnoreCaseAndAddress_CountryIgnoreCase(Customer customer, String street, String ward, String district, String city, String country);
}
