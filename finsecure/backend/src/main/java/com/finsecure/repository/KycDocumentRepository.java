package com.finsecure.repository;

import com.finsecure.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByCustomerId(Long customerId);

    List<KycDocument> findByVerificationStatus(KycDocument.VerificationStatus status);

    @Query("""
        SELECT k FROM KycDocument k
        WHERE k.verificationStatus = 'PENDING'
        ORDER BY k.uploadDate ASC
    """)
    List<KycDocument> findPendingDocuments();
}
