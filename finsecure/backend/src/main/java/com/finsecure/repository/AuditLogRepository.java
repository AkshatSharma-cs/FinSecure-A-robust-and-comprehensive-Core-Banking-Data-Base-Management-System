package com.finsecure.repository;

import com.finsecure.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByActionTypeOrderByTimestampDesc(AuditLog.ActionType actionType);

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.timestamp BETWEEN ?1 AND ?2
        ORDER BY a.timestamp DESC
    """)
    List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
