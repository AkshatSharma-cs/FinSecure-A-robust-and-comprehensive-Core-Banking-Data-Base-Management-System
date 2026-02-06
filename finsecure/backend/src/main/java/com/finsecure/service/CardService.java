package com.finsecure.service;

import static com.finsecure.dto.DTOs.*;
import com.finsecure.entity.Card;
import com.finsecure.entity.Customer;
import com.finsecure.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    public Card performCardAction(CardActionRequest request, Customer customer) {

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // Ownership check
        if (!card.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new RuntimeException("Card does not belong to customer");
        }

        String action = request.getAction().toUpperCase();

        switch (action) {

            case "BLOCK" -> {
                if (card.getStatus() == Card.Status.BLOCKED) {
                    throw new RuntimeException("Card is already blocked");
                }
                card.setStatus(Card.Status.BLOCKED);
            }

            case "UNBLOCK" -> {
                if (card.getStatus() != Card.Status.BLOCKED) {
                    throw new RuntimeException("Only blocked cards can be unblocked");
                }
                card.setStatus(Card.Status.ACTIVE);
            }

            case "REPLACE" -> {
                card.setStatus(Card.Status.REPLACED);
                card.setReplacementRequested(true);
            }

            default -> throw new RuntimeException("Invalid card action");
        }

        return cardRepository.save(card);
    }
}
