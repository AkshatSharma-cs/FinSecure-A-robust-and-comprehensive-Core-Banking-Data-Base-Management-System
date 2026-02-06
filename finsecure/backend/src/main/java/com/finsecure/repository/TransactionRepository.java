package com.finsecure.repository;

import com.finsecure.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.account.accountId = ?1
        AND t.transactionDate BETWEEN ?2 AND ?3
        ORDER BY t.transactionDate DESC
    """)
    List<Transaction> findByAccountIdAndDateRange(
        Long accountId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.account.customer.customerId = ?1
        ORDER BY t.transactionDate DESC
    """)
    List<Transaction> findByCustomerId(Long customerId);
}
