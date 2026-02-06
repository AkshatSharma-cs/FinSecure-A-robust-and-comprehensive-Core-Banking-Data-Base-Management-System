package com.finsecure.repository;

import com.finsecure.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);
    List<Customer> findByKycStatus(Customer.KycStatus kycStatus);
    List<Customer> findByAccountType(Customer.AccountType accountType);
    Optional<Customer> findByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE c.kycStatus = 'PENDING' ORDER BY c.createdAt ASC")
    List<Customer> findPendingKycCustomers();
}
