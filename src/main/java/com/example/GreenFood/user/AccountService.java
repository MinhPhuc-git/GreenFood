package com.example.GreenFood.user;

import com.example.GreenFood.user.AddressDTO;
import com.example.GreenFood.user.UpdateProfileDTO;
import com.example.GreenFood.model.Address;
import com.example.GreenFood.model.Customer;
import com.example.GreenFood.model.User;
import com.example.GreenFood.model.CustomerAddress;
import com.example.GreenFood.model.CustomerAddressId;
import com.example.GreenFood.user.AddressRepository;
import com.example.GreenFood.user.CustomerAddressRepository;
import com.example.GreenFood.user.CustomerRepository;
import com.example.GreenFood.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AccountService(CustomerRepository customerRepository,
                          UserRepository userRepository,
                          AddressRepository addressRepository,
                          CustomerAddressRepository customerAddressRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public Customer updateProfile(int userId, UpdateProfileDTO dto) {
        // 1. Kiểm tra User và đổi mật khẩu
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (dto.getOldPassword() != null && !dto.getOldPassword().isBlank() &&
            dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            
            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPwd()) && !dto.getOldPassword().equals(user.getPwd())) {
                throw new RuntimeException("Mật khẩu cũ không chính xác");
            }
            if (dto.getNewPassword().length() < 6) {
                throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự");
            }
            user.setPwd(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);
        }

        // 2. Cập nhật thông tin Customer (Họ tên, SĐT)
        String phone = dto.getPhone() != null ? dto.getPhone().trim() : "";
        if (!phone.isBlank()) {
            if (customerRepository.existsByPhoneAndIdNot(phone, userId)) {
                throw new RuntimeException("Số điện thoại đã được sử dụng bởi tài khoản khác");
            }
        }

        Customer customer = customerRepository.findById(userId).orElse(null);
        if (customer == null) {
            // Chưa có trong bảng customer -> Khởi tạo
            String nameToSave = (dto.getName() != null && !dto.getName().isBlank()) ? dto.getName().trim() : user.getAccount();
            customerRepository.insertCustomer(userId, nameToSave, phone);
            // Fetch lại để có đối tượng entity
            customer = customerRepository.findById(userId).orElseThrow(() -> new RuntimeException("Lỗi khởi tạo hồ sơ khách hàng"));
        } else {
            if (dto.getName() != null && !dto.getName().isBlank()) {
                customer.setName(dto.getName().trim());
            }
            if (!phone.isBlank()) {
                customer.setPhone(phone);
            }
            customer = customerRepository.save(customer);
        }

        // 3. Cập nhật / Khởi tạo địa chỉ mặc định
        if (dto.getStreet() != null && !dto.getStreet().isBlank() &&
            dto.getDistrict() != null && !dto.getDistrict().isBlank() &&
            dto.getCity() != null && !dto.getCity().isBlank()) {
            
            // Tìm địa chỉ mặc định hiện tại
            CustomerAddress defaultCa = customerAddressRepository.findByCustomerAndIsDefaultTrue(customer).orElse(null);
            
            if (defaultCa != null) {
                // Đã có -> Cập nhật trực tiếp
                Address addr = defaultCa.getAddress();
                addr.setStreet(dto.getStreet().trim());
                addr.setWard(dto.getWard() != null ? dto.getWard().trim() : "");
                addr.setDistrict(dto.getDistrict().trim());
                addr.setCity(dto.getCity().trim());
                addr.setCountry("Việt Nam");
                addressRepository.save(addr);
            } else {
                // Chưa có -> Tạo mới
                Address addr = new Address();
                addr.setStreet(dto.getStreet().trim());
                addr.setWard(dto.getWard() != null ? dto.getWard().trim() : "");
                addr.setDistrict(dto.getDistrict().trim());
                addr.setCity(dto.getCity().trim());
                addr.setCountry("Việt Nam");
                addr = addressRepository.save(addr);

                CustomerAddress ca = new CustomerAddress();
                ca.setId(new CustomerAddressId(customer.getId(), addr.getId()));
                ca.setCustomer(customer);
                ca.setAddress(addr);
                ca.setIsDefault(true);
                customerAddressRepository.save(ca);
            }
        }

        return customer;
    }

    @Transactional
    public CustomerAddress addAddress(int customerId, AddressDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        // Trim and normalize fields for duplicate check
        String street = dto.getStreet() != null ? dto.getStreet().trim().toLowerCase() : "";
        String ward = dto.getWard() != null ? dto.getWard().trim().toLowerCase() : "";
        String district = dto.getDistrict() != null ? dto.getDistrict().trim().toLowerCase() : "";
        String city = dto.getCity() != null ? dto.getCity().trim().toLowerCase() : "";
        String country = dto.getCountry() != null ? dto.getCountry().trim().toLowerCase() : "";

        boolean exists = customerAddressRepository
                .existsByCustomerAndAddress_StreetIgnoreCaseAndAddress_WardIgnoreCaseAndAddress_DistrictIgnoreCaseAndAddress_CityIgnoreCaseAndAddress_CountryIgnoreCase(
                        customer, street, ward, district, city, country);
        if (exists) {
            throw new RuntimeException("Địa chỉ đã tồn tại");
        }

        // Create or reuse Address entity
        Address address = new Address();
        address.setStreet(dto.getStreet().trim());
        address.setWard(dto.getWard().trim());
        address.setDistrict(dto.getDistrict().trim());
        address.setCity(dto.getCity().trim());
        address.setCountry(dto.getCountry().trim());
        address = addressRepository.save(address);

        // Determine default flag
        List<CustomerAddress> existing = customerAddressRepository.findByCustomer(customer);
        boolean isDefault = existing.isEmpty() ? true : Boolean.TRUE.equals(dto.getIsDefault());

        CustomerAddressId caId = new CustomerAddressId(customer.getId(), address.getId());
        CustomerAddress ca = new CustomerAddress();
        ca.setId(caId);
        ca.setCustomer(customer);
        ca.setAddress(address);
        ca.setIsDefault(isDefault);
        return customerAddressRepository.save(ca);
    }

    public List<CustomerAddress> getAddresses(int customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        return customerAddressRepository.findByCustomer(customer);
    }
}
