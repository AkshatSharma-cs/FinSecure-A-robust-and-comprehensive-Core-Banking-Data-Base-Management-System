package com.finsecure.repository;

import com.finsecure.entity.Card;
import com.finsecure.entity.Card.CardStatus;
import com.finsecure.entity.Card.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByAccountId(Long accountId);

    List<Card> findByAccountIdAndStatus(Long accountId, CardStatus status);

    List<Card> findByAccountCustomerId(Long customerId);

    @Query("SELECT c FROM Card c WHERE c.account.customer.id = :customerId AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByCustomerId(Long customerId);

    boolean existsByAccountIdAndCardType(Long accountId, CardType cardType);
}
