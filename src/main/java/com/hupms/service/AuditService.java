package com.hupms.service;

import com.hupms.dto.response.AuditLogResponse;
import com.hupms.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService implements AuditableService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(Long actorId, String action, String entityType, Long entityId, String details) {
        auditLogRepository.save(actorId, action, entityType, entityId, details);
    }

    public List<AuditLogResponse> list(String entityType, Long entityId) {
        return auditLogRepository.findAll(entityType, entityId).stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
