package com.hupms.repository;

import com.hupms.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class AuditLogRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<AuditLog> mapper = (rs, rowNum) -> {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));
        long actorId = rs.getLong("actor_id");
        log.setActorId(rs.wasNull() ? null : actorId);
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        long entityId = rs.getLong("entity_id");
        log.setEntityId(rs.wasNull() ? null : entityId);
        log.setDetails(rs.getString("details"));
        Timestamp performed = rs.getTimestamp("performed_at");
        log.setPerformedAt(performed == null ? null : performed.toLocalDateTime());
        log.setCreatedAt(log.getPerformedAt());
        return log;
    };

    public AuditLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(Long actorId, String action, String entityType, Long entityId, String details) {
        jdbc.update("""
                INSERT INTO audit_logs (actor_id, action, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, ?)
                """, actorId, action, entityType, entityId, details);
    }

    public List<AuditLog> findAll(String entityType, Long entityId) {
        if (entityType != null && entityId != null) {
            return jdbc.query("SELECT * FROM audit_logs WHERE entity_type = ? AND entity_id = ? ORDER BY performed_at DESC",
                    mapper, entityType, entityId);
        }
        if (entityType != null) {
            return jdbc.query("SELECT * FROM audit_logs WHERE entity_type = ? ORDER BY performed_at DESC", mapper, entityType);
        }
        return jdbc.query("SELECT * FROM audit_logs ORDER BY performed_at DESC", mapper);
    }
}
