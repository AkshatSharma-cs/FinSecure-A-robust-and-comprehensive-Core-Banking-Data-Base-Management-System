package com.finsecure.repository;

import com.finsecure.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByStatus(Loan.Status status);

    @Query("""
        SELECT l FROM Loan l
        WHERE l.customer.customerId = ?1
        AND l.status IN ('ACTIVE', 'DISBURSED')
    """)
    List<Loan> findActiveLoansByCustomerId(Long customerId);
}
