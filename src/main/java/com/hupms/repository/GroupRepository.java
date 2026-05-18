package com.hupms.repository;

import com.hupms.model.Group;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class GroupRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Group> mapper = (rs, rowNum) -> {
        Group group = new Group();
        group.setId(rs.getLong("id"));
        group.setGroupName(rs.getString("group_name"));
        group.setPackageId(rs.getLong("package_id"));
        group.setAgentId(rs.getLong("agent_id"));
        group.setMaxSize(rs.getInt("max_size"));
        Timestamp created = rs.getTimestamp("created_at");
        group.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return group;
    };

    public GroupRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long save(Group group) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO groups (group_name, package_id, agent_id, max_size)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, group.getGroupName());
            ps.setLong(2, group.getPackageId());
            ps.setLong(3, group.getAgentId());
            ps.setInt(4, group.getMaxSize());
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    public List<Group> findAll() {
        return jdbc.query("SELECT * FROM groups ORDER BY id", mapper);
    }

    public List<Group> findByAgentId(Long agentId) {
        return jdbc.query("SELECT * FROM groups WHERE agent_id = ? ORDER BY id", mapper, agentId);
    }

    public Optional<Group> findById(Long id) {
        return jdbc.query("SELECT * FROM groups WHERE id = ?", mapper, id).stream().findFirst();
    }

    public Optional<Group> lockById(Long id) {
        return jdbc.query("SELECT * FROM groups WHERE id = ? FOR UPDATE", mapper, id).stream().findFirst();
    }

    public boolean existsByIdAndAgentId(Long id, Long agentId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM groups WHERE id = ? AND agent_id = ?", Integer.class, id, agentId);
        return count != null && count > 0;
    }

    public int countByPackageId(Long packageId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM groups WHERE package_id = ?", Integer.class, packageId);
        return count == null ? 0 : count;
    }

    public void update(Group group) {
        jdbc.update("UPDATE groups SET group_name = ?, max_size = ? WHERE id = ?",
                group.getGroupName(), group.getMaxSize(), group.getId());
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM groups WHERE id = ?", id);
    }
}
