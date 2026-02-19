package com.finsecure.repository;

import com.finsecure.entity.KycDocument;
import com.finsecure.entity.KycDocument.DocumentStatus;
import com.finsecure.entity.KycDocument.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByCustomerId(Long customerId);

    Optional<KycDocument> findByCustomerIdAndDocumentType(Long customerId, DocumentType documentType);

    Page<KycDocument> findByStatus(DocumentStatus status, Pageable pageable);

    @Query("SELECT k FROM KycDocument k WHERE k.status = 'UPLOADED' OR k.status = 'UNDER_REVIEW' ORDER BY k.createdAt ASC")
    Page<KycDocument> findPendingDocuments(Pageable pageable);

    long countByCustomerIdAndStatus(Long customerId, DocumentStatus status);
}
