package com.hupms.dto.response;

import com.hupms.model.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, Long actorId, String action, String entityType, Long entityId,
                               String details, LocalDateTime performedAt) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getActorId(), log.getAction(), log.getEntityType(),
                log.getEntityId(), log.getDetails(), log.getPerformedAt());
    }
}
