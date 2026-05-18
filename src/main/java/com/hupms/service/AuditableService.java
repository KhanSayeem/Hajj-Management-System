package com.hupms.service;

public interface AuditableService {
    void log(Long actorId, String action, String entityType, Long entityId, String details);
}
