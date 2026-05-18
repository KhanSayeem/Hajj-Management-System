package com.hupms.repository;

import com.hupms.enums.Gender;
import com.hupms.enums.PilgrimStatus;
import com.hupms.model.Pilgrim;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PilgrimRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Pilgrim> mapper = (rs, rowNum) -> {
        Pilgrim pilgrim = new Pilgrim();
        pilgrim.setId(rs.getLong("id"));
        pilgrim.setUserId(rs.getLong("user_id"));
        long groupId = rs.getLong("group_id");
        pilgrim.setGroupId(rs.wasNull() ? null : groupId);
        pilgrim.setPassportNumber(rs.getString("passport_number"));
        Date dob = rs.getDate("date_of_birth");
        pilgrim.setDateOfBirth(dob == null ? null : dob.toLocalDate());
        pilgrim.setNationality(rs.getString("nationality"));
        pilgrim.setPhone(rs.getString("phone"));
        pilgrim.setGender(Gender.valueOf(rs.getString("gender")));
        long mahramId = rs.getLong("mahram_id");
        pilgrim.setMahramId(rs.wasNull() ? null : mahramId);
        pilgrim.setStatus(PilgrimStatus.valueOf(rs.getString("status")));
        Timestamp created = rs.getTimestamp("registered_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        pilgrim.setCreatedAt(created == null ? null : created.toLocalDateTime());
        pilgrim.setUpdatedAt(updated == null ? null : updated.toLocalDateTime());
        return pilgrim;
    };

    public PilgrimRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long save(Pilgrim pilgrim) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO pilgrims (user_id, group_id, passport_number, date_of_birth, nationality, phone, gender, mahram_id, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, pilgrim.getUserId());
            setNullableLong(ps, 2, pilgrim.getGroupId());
            ps.setString(3, pilgrim.getPassportNumber());
            ps.setDate(4, pilgrim.getDateOfBirth() == null ? null : Date.valueOf(pilgrim.getDateOfBirth()));
            ps.setString(5, pilgrim.getNationality());
            ps.setString(6, pilgrim.getPhone());
            ps.setString(7, pilgrim.getGender().name());
            setNullableLong(ps, 8, pilgrim.getMahramId());
            ps.setString(9, pilgrim.getStatus().name());
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    public List<Pilgrim> findAll() {
        return jdbc.query("SELECT * FROM pilgrims ORDER BY id", mapper);
    }

    public List<Pilgrim> findByGroupId(Long groupId) {
        return jdbc.query("SELECT * FROM pilgrims WHERE group_id = ? ORDER BY id", mapper, groupId);
    }

    public List<Pilgrim> findByAgentId(Long agentId) {
        return jdbc.query("""
                SELECT p.* FROM pilgrims p
                JOIN groups g ON g.id = p.group_id
                WHERE g.agent_id = ?
                ORDER BY p.id
                """, mapper, agentId);
    }

    public Optional<Pilgrim> findById(Long id) {
        return jdbc.query("SELECT * FROM pilgrims WHERE id = ?", mapper, id).stream().findFirst();
    }

    public Optional<Pilgrim> findByUserId(Long userId) {
        return jdbc.query("SELECT * FROM pilgrims WHERE user_id = ?", mapper, userId).stream().findFirst();
    }

    public boolean existsByPassportNumber(String passportNumber) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM pilgrims WHERE passport_number = ?", Integer.class, passportNumber);
        return count != null && count > 0;
    }

    public int countByGroupId(Long groupId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM pilgrims WHERE group_id = ?", Integer.class, groupId);
        return count == null ? 0 : count;
    }

    public void update(Pilgrim pilgrim) {
        jdbc.update("""
                UPDATE pilgrims
                SET date_of_birth=?, nationality=?, phone=?, gender=?, mahram_id=?, updated_at=NOW()
                WHERE id=?
                """, ps -> {
            ps.setDate(1, pilgrim.getDateOfBirth() == null ? null : Date.valueOf(pilgrim.getDateOfBirth()));
            ps.setString(2, pilgrim.getNationality());
            ps.setString(3, pilgrim.getPhone());
            ps.setString(4, pilgrim.getGender().name());
            setNullableLong(ps, 5, pilgrim.getMahramId());
            ps.setLong(6, pilgrim.getId());
        });
    }

    public void updateStatus(Long id, PilgrimStatus status) {
        jdbc.update("UPDATE pilgrims SET status = ?, updated_at = NOW() WHERE id = ?", status.name(), id);
    }

    public void assignGroup(Long id, Long groupId) {
        jdbc.update("UPDATE pilgrims SET group_id = ?, updated_at = NOW() WHERE id = ?", groupId, id);
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM pilgrims WHERE id = ?", id);
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws java.sql.SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setLong(index, value);
        }
    }
}
