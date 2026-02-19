package com.finsecure.dto;

import com.finsecure.entity.KycDocument.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycDocumentUploadRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotBlank(message = "File path is required")
    private String filePath;

    private String fileName;
    private String mimeType;
}
