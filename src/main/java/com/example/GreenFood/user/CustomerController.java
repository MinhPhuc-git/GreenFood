package com.example.GreenFood.user;

import com.example.GreenFood.model.Address;
import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.CustomerAddress;
import com.example.GreenFood.model.CustomerAddressId;
import com.example.GreenFood.user.AddressRepository;
import com.example.GreenFood.user.CustomerAddressRepository;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.loyalty.MembershipCardRepository;
import com.example.GreenFood.loyalty.RewardPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers/me")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MembershipCardRepository membershipCardRepository;

    @Autowired
    private RewardPointService rewardPointService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CustomerAddressRepository customerAddressRepository;

    private Customer getAuthenticatedCustomer(Authentication authentication) {
        String account = authentication.getName();
        return customerRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    @GetMapping
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        String tier = membershipCardRepository.findByCustomer(customer)
                .map(card -> card.getTier())
                .orElse("REGULAR");
        int points = rewardPointService.getValidPoints(customer);

        return ResponseEntity.ok(Map.of(
                "name", customer.getName(),
                "phone", customer.getPhone(),
                "account", customer.getAccount(),
                "tier", tier,
                "rewardPoints", points,
                "loyaltyPoints", points
        ));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        if (body.containsKey("name")) customer.setName(body.get("name"));
        if (body.containsKey("phone")) customer.setPhone(body.get("phone"));
        customerRepository.save(customer);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getMyAddresses(Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        List<Map<String, Object>> addresses = customerAddressRepository.findByCustomer(customer).stream().map(ca -> {
            Address a = ca.getAddress();
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", a.getId());
            map.put("street", a.getStreet());
            map.put("ward", a.getWard());
            map.put("district", a.getDistrict());
            map.put("city", a.getCity());
            map.put("country", a.getCountry());
            map.put("isDefault", ca.getIsDefault() != null && ca.getIsDefault());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(@RequestBody Address address, Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        Address savedAddress = addressRepository.save(address);
        CustomerAddress ca = new CustomerAddress();
        CustomerAddressId caId = new CustomerAddressId(customer.getId(), savedAddress.getId());
        ca.setId(caId);
        ca.setCustomer(customer);
        ca.setAddress(savedAddress);
        ca.setIsDefault(false);
        customerAddressRepository.save(ca);
        return ResponseEntity.ok("Address added successfully");
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable int id, Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        CustomerAddressId caId = new CustomerAddressId(customer.getId(), id);
        customerAddressRepository.findById(caId).ifPresent(ca -> {
            customerAddressRepository.delete(ca);
            addressRepository.deleteById(id);
        });
        return ResponseEntity.ok("Address deleted successfully");
    }

    // ─── Lấy thông tin profile theo ID (frontend dùng customerId từ localStorage) ───
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable int id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy khách hàng"));
        }
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("id",        customer.getId());
        data.put("name",      customer.getName());
        data.put("phone",     customer.getPhone());
        data.put("account",   customer.getAccount());
        data.put("status",    customer.getStatus());
        data.put("createdAt", customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : "");
        return ResponseEntity.ok(data);
    }
}
