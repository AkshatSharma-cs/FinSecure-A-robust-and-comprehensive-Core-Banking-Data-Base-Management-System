package com.finsecure.service;

import com.finsecure.entity.AuditLog;
import com.finsecure.entity.AuditLog.AuditResult;
import com.finsecure.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void logAction(Long userId, String username, String action, String resource, String resourceId, String details, AuditResult result) {
        try {
            String ipAddress = extractIpAddress();
            String userAgent = extractUserAgent();

            AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .result(result)
                .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    @Async
    @Transactional
    public void logSuccess(Long userId, String username, String action, String resource, String resourceId, String details) {
        logAction(userId, username, action, resource, resourceId, details, AuditResult.SUCCESS);
    }

    @Async
    @Transactional
    public void logFailure(Long userId, String username, String action, String resource, String resourceId, String errorMessage) {
        AuditLog auditLog = AuditLog.builder()
            .userId(userId != null ? userId : 0L)
            .username(username != null ? username : "UNKNOWN")
            .action(action)
            .resource(resource)
            .resourceId(resourceId)
            .errorMessage(errorMessage)
            .ipAddress(extractIpAddress())
            .userAgent(extractUserAgent())
            .result(AuditResult.FAILURE)
            .build();

        auditLogRepository.save(auditLog);
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanOldLogs() {
        // Keep logs for 90 days
        log.info("Audit log cleanup scheduled task runs at 2 AM daily");
    }

    private String extractIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not extract IP address: {}", e.getMessage());
        }
        return "UNKNOWN";
    }

    private String extractUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not extract User-Agent: {}", e.getMessage());
        }
        return "UNKNOWN";
    }
}
