package com.hupms.repository;

import com.hupms.enums.PackageType;
import com.hupms.model.TravelPackage;
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
public class PackageRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<TravelPackage> mapper = (rs, rowNum) -> {
        TravelPackage value = new TravelPackage();
        value.setId(rs.getLong("id"));
        value.setName(rs.getString("name"));
        value.setType(PackageType.valueOf(rs.getString("type")));
        value.setYear(rs.getInt("year"));
        value.setCapacity(rs.getInt("capacity"));
        value.setPriceUsd(rs.getBigDecimal("price_usd"));
        Date departure = rs.getDate("departure_date");
        Date returned = rs.getDate("return_date");
        value.setDepartureDate(departure == null ? null : departure.toLocalDate());
        value.setReturnDate(returned == null ? null : returned.toLocalDate());
        value.setCreatedBy(rs.getLong("created_by"));
        Timestamp created = rs.getTimestamp("created_at");
        value.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return value;
    };

    public PackageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long save(TravelPackage value) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO packages (name, type, year, capacity, price_usd, departure_date, return_date, created_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            bind(value, ps);
            ps.setLong(8, value.getCreatedBy());
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    public List<TravelPackage> findAll() {
        return jdbc.query("SELECT * FROM packages ORDER BY id", mapper);
    }

    public Optional<TravelPackage> findById(Long id) {
        return jdbc.query("SELECT * FROM packages WHERE id = ?", mapper, id).stream().findFirst();
    }

    public void update(TravelPackage value) {
        jdbc.update("""
                UPDATE packages SET name=?, type=?, year=?, capacity=?, price_usd=?, departure_date=?, return_date=?
                WHERE id=?
                """, ps -> {
            bind(value, ps);
            ps.setLong(8, value.getId());
        });
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM packages WHERE id = ?", id);
    }

    private void bind(TravelPackage value, PreparedStatement ps) throws java.sql.SQLException {
        ps.setString(1, value.getName());
        ps.setString(2, value.getType().name());
        ps.setInt(3, value.getYear());
        ps.setInt(4, value.getCapacity());
        ps.setBigDecimal(5, value.getPriceUsd());
        ps.setDate(6, value.getDepartureDate() == null ? null : Date.valueOf(value.getDepartureDate()));
        ps.setDate(7, value.getReturnDate() == null ? null : Date.valueOf(value.getReturnDate()));
    }
}
