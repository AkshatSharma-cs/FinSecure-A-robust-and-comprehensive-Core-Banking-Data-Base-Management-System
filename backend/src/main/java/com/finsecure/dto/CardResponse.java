package com.finsecure.dto;

import com.finsecure.entity.Card.CardStatus;
import com.finsecure.entity.Card.CardType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CardResponse {

    private Long id;
    private String accountNumber;
    private CardType cardType;
    private String maskedCardNumber;
    private String cardHolderName;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal creditLimit;
    private BigDecimal availableLimit;
    private Boolean internationalEnabled;
    private Boolean onlineEnabled;
    private Boolean contactlessEnabled;
    private LocalDateTime createdAt;
}
