package com.finsecure.repository;

import com.finsecure.entity.Loan;
import com.finsecure.entity.Loan.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    List<Loan> findByCustomerId(Long customerId);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    Page<Loan> findByCustomerIdAndStatus(Long customerId, LoanStatus status, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.status IN ('APPLIED', 'UNDER_REVIEW') ORDER BY l.createdAt ASC")
    Page<Loan> findPendingLoans(Pageable pageable);

    boolean existsByLoanNumber(String loanNumber);
}
