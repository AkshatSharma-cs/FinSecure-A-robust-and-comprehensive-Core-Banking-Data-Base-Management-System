package com.finsecure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycVerificationRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotBlank(message = "Action is required")
    private String action; // APPROVE or REJECT

    private String rejectionReason;
}
