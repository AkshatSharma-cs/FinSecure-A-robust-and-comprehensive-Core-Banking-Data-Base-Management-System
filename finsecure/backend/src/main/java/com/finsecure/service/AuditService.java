package com.finsecure.service;

import com.finsecure.entity.AuditLog;
import com.finsecure.entity.User;
import com.finsecure.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(
            Long userId,
            AuditLog.ActionType actionType,
            String entityType,
            Long entityId,
            String description,
            HttpServletRequest request
    ) {
        AuditLog log = new AuditLog();
        if (userId != null) {
            User user = new User();
            user.setUserId(userId);
            log.setUser(user);
        }
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);
    }
}
