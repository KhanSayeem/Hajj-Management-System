package com.hupms.repository;

import com.hupms.enums.Role;
import com.hupms.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        user.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return user;
    };

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long save(User user) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO users (full_name, email, password_hash, role, is_active)
                    VALUES (?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    public Optional<User> findByEmail(String email) {
        return jdbc.query("SELECT * FROM users WHERE email = ?", mapper, email).stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", mapper, id).stream().findFirst();
    }
}
