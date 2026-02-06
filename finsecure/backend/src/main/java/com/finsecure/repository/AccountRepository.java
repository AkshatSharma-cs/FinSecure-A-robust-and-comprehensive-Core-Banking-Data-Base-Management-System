package com.finsecure.repository;

import com.finsecure.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByCustomerId(Long customerId);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerIdAndStatus(Long customerId, Account.Status status);

    @Query("SELECT a FROM Account a WHERE a.customer.customerId = ?1 AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByCustomerId(Long customerId);
}
