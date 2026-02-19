package com.finsecure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CardActionRequest {

    @NotNull(message = "Card ID is required")
    private Long cardId;

    @NotBlank(message = "Action is required")
    private String action; // BLOCK, UNBLOCK, ENABLE_INTERNATIONAL, DISABLE_INTERNATIONAL, ENABLE_ONLINE, DISABLE_ONLINE

    private String otpCode;
    private String reason;
}
