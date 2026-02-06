package com.finsecure.repository;

import com.finsecure.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByCustomerId(Long customerId);

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByCustomerIdAndStatus(Long customerId, Card.Status status);

    @Query("SELECT c FROM Card c WHERE c.customer.customerId = ?1 AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByCustomerId(Long customerId);
}
