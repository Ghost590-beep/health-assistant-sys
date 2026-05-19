package com.health.backend.repository;

import com.health.backend.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setRoleId(rs.getInt("role_id"));
        user.setGenderCode(rs.getString("gender_code"));
        return user;
    };

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbc.query(sql, userRowMapper, email)
                .stream()
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, first_name, last_name, role_id, gender_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoleId(),
                user.getGenderCode());
    }

    public void savePatient(int userId, String dateOfBirth, String bloodCode, String address) {
        String sql = "INSERT INTO patients (user_id, date_of_birth, blood_code, address) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, userId, dateOfBirth, bloodCode, address);
    }

    public void saveContact(int userId, String type, String value) {
        String sql = "INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, userId, type, value, "PHONE".equals(type));
    }

    public int getLastInsertId() {
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }
    public void saveDoctor(int userId, String licenseNumber, int yearsOfExperience, String clinicAddress) {
        String sql = "INSERT INTO doctors (user_id, license_number, years_of_experience, clinic_address) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, userId, licenseNumber, yearsOfExperience, clinicAddress);
    }

    public int getLastDoctorId() {
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    public void saveDoctorSpecialization(int doctorId, int specializationId) {
        String sql = "INSERT INTO doctor_specializations (doctor_id, specialization_id) VALUES (?, ?)";
        jdbc.update(sql, doctorId, specializationId);
    }

    public void saveAdmin(int userId, String department, int accessLevel) {
        String sql = "INSERT INTO admins (user_id, department, access_level) VALUES (?, ?, ?)";
        jdbc.update(sql, userId, department, accessLevel);
    }

    public Integer getSpecializationIdByName(String name) {
        String sql = "SELECT specialization_id FROM specializations WHERE specialization_name = ?";
        return jdbc.queryForObject(sql, Integer.class, name);
    }
}