package com.finsecure.service;

import com.finsecure.dto.CardActionRequest;
import com.finsecure.dto.CardResponse;
import com.finsecure.entity.*;
import com.finsecure.entity.Card.CardStatus;
import com.finsecure.entity.Card.CardType;
import com.finsecure.repository.AccountRepository;
import com.finsecure.repository.CardRepository;
import com.finsecure.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    private final BCryptPasswordEncoder cvvEncoder = new BCryptPasswordEncoder();

    @Transactional
    public CardResponse issueDebitCard(Long accountId, String userEmail) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getCustomer().getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("Unauthorized");
        }

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account must be active to issue a card");
        }

        // Check KYC
        if (account.getCustomer().getKycStatus() != Customer.KycStatus.APPROVED) {
            throw new IllegalStateException("KYC must be approved to issue a card");
        }

        if (cardRepository.existsByAccountIdAndCardType(accountId, CardType.DEBIT)) {
            throw new IllegalStateException("A debit card already exists for this account");
        }

        String cardNumber = generateCardNumber();
        String cvv = generateCvv();

        Card card = Card.builder()
            .account(account)
            .cardType(CardType.DEBIT)
            .maskedCardNumber(maskCardNumber(cardNumber))
            .cardNumberHash(cvvEncoder.encode(cardNumber))
            .cardHolderName(account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName())
            .expiryDate(LocalDate.now().plusYears(5))
            .cvvHash(cvvEncoder.encode(cvv))
            .status(CardStatus.ACTIVE)
            .build();

        card = cardRepository.save(card);

        notificationService.createNotification(
            account.getCustomer().getUser().getId(),
            com.finsecure.entity.Notification.NotificationType.CARD,
            "Debit Card Issued",
            "Your debit card " + card.getMaskedCardNumber() + " has been issued successfully.",
            card.getId().toString(), "CARD"
        );

        return mapToResponse(card);
    }

    @Transactional
    public CardResponse issueCreditCard(Long accountId, BigDecimal creditLimit, String userEmail) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getCustomer().getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("Unauthorized");
        }

        if (account.getCustomer().getKycStatus() != Customer.KycStatus.APPROVED) {
            throw new IllegalStateException("KYC must be approved to issue a credit card");
        }

        String cardNumber = generateCardNumber();
        String cvv = generateCvv();

        Card card = Card.builder()
            .account(account)
            .cardType(CardType.CREDIT)
            .maskedCardNumber(maskCardNumber(cardNumber))
            .cardNumberHash(cvvEncoder.encode(cardNumber))
            .cardHolderName(account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName())
            .expiryDate(LocalDate.now().plusYears(5))
            .cvvHash(cvvEncoder.encode(cvv))
            .status(CardStatus.ACTIVE)
            .creditLimit(creditLimit)
            .availableLimit(creditLimit)
            .build();

        card = cardRepository.save(card);
        return mapToResponse(card);
    }

    @Transactional
    public CardResponse performCardAction(CardActionRequest request, String userEmail) {
        Card card = cardRepository.findById(request.getCardId())
            .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (!card.getAccount().getCustomer().getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("Unauthorized");
        }

        switch (request.getAction().toUpperCase()) {
            case "BLOCK" -> card.setStatus(CardStatus.BLOCKED);
            case "UNBLOCK" -> {
                if (card.getStatus() != CardStatus.BLOCKED) {
                    throw new IllegalStateException("Card is not blocked");
                }
                card.setStatus(CardStatus.ACTIVE);
            }
            case "ENABLE_INTERNATIONAL" -> card.setInternationalEnabled(true);
            case "DISABLE_INTERNATIONAL" -> card.setInternationalEnabled(false);
            case "ENABLE_ONLINE" -> card.setOnlineEnabled(true);
            case "DISABLE_ONLINE" -> card.setOnlineEnabled(false);
            default -> throw new IllegalArgumentException("Unknown card action: " + request.getAction());
        }

        card = cardRepository.save(card);

        notificationService.createNotification(
            card.getAccount().getCustomer().getUser().getId(),
            com.finsecure.entity.Notification.NotificationType.CARD,
            "Card Updated",
            "Card action " + request.getAction() + " performed on card " + card.getMaskedCardNumber(),
            card.getId().toString(), "CARD"
        );

        return mapToResponse(card);
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCustomerCards(String userEmail) {
        Customer customer = customerRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return cardRepository.findByAccountCustomerId(customer.getId())
            .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("4"); // Visa prefix
        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateCvv() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

    private String maskCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private CardResponse mapToResponse(Card card) {
        return CardResponse.builder()
            .id(card.getId())
            .accountNumber(card.getAccount().getAccountNumber())
            .cardType(card.getCardType())
            .maskedCardNumber(card.getMaskedCardNumber())
            .cardHolderName(card.getCardHolderName())
            .expiryDate(card.getExpiryDate())
            .status(card.getStatus())
            .creditLimit(card.getCreditLimit())
            .availableLimit(card.getAvailableLimit())
            .internationalEnabled(card.getInternationalEnabled())
            .onlineEnabled(card.getOnlineEnabled())
            .contactlessEnabled(card.getContactlessEnabled())
            .createdAt(card.getCreatedAt())
            .build();
    }
}
