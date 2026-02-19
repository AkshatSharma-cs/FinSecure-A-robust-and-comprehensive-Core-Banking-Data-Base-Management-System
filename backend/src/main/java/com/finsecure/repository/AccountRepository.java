package com.finsecure.repository;

import com.finsecure.entity.Account;
import com.finsecure.entity.Account.AccountStatus;
import com.finsecure.entity.Account.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    List<Account> findByCustomerIdAndStatus(Long customerId, AccountStatus status);

    List<Account> findByCustomerIdAndAccountType(Long customerId, AccountType accountType);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.customer.user.email = :email AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByUserEmail(String email);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.customer.id = :customerId AND a.status = 'ACTIVE'")
    long countActiveAccountsByCustomerId(Long customerId);
}
