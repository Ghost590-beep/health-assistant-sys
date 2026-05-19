package com.health.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final JdbcTemplate jdbc;

    public PatientController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.first_name, u.last_name,
                   u.gender_code, p.patient_id, p.date_of_birth, p.blood_code, p.address
            FROM users u
            JOIN patients p ON u.user_id = p.user_id
            WHERE u.role_id = 1
            ORDER BY u.first_name
        """;
        return jdbc.queryForList(sql);
    }

    @GetMapping("/{userId}")
    public Map<String, Object> getById(@PathVariable int userId) {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.first_name, u.last_name,
                   u.gender_code, p.patient_id, p.date_of_birth, p.blood_code, p.address,
                   COALESCE(c.contact_value, '') AS phone
            FROM users u
            JOIN patients p ON u.user_id = p.user_id
            LEFT JOIN contacts c ON u.user_id = c.user_id AND c.contact_type = 'PHONE' AND c.is_primary = TRUE
            WHERE u.user_id = ?
        """;
        return jdbc.queryForMap(sql, userId);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updatePatient(@PathVariable int userId, @RequestBody Map<String, Object> dto) {
        try {
            String firstName = ((String) dto.get("firstName")).trim();
            String lastName = ((String) dto.get("lastName")).trim();
            String email = (String) dto.get("email");
            String phone = (String) dto.getOrDefault("phone", "");
            String address = (String) dto.getOrDefault("address", "");

            // 1. Update users table
            String updateUserSql = "UPDATE users SET first_name = ?, last_name = ?, email = ? WHERE user_id = ?";
            jdbc.update(updateUserSql, firstName, lastName, email, userId);

            // 2. Update patients address
            if (!address.isEmpty()) {
                String updatePatientSql = "UPDATE patients SET address = ? WHERE user_id = ?";
                jdbc.update(updatePatientSql, address, userId);
            }

            // 3. Update phone contact
            if (!phone.isEmpty()) {
                String checkSql = "SELECT COUNT(*) FROM contacts WHERE user_id = ? AND contact_type = 'PHONE'";
                int exists = jdbc.queryForObject(checkSql, Integer.class, userId);

                if (exists > 0) {
                    jdbc.update("UPDATE contacts SET contact_value = ? WHERE user_id = ? AND contact_type = 'PHONE'",
                            phone, userId);
                } else {
                    jdbc.update(
                            "INSERT INTO contacts (user_id, contact_type, contact_value, is_primary) VALUES (?, 'PHONE', ?, TRUE)",
                            userId, phone);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile updated successfully",
                    "firstName", firstName,
                    "lastName", lastName,
                    "email", email
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error updating profile: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable int userId, @RequestBody Map<String, String> request) {
        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            // Verify old password (in production, use bcrypt)
            String sql = "SELECT password_hash FROM users WHERE user_id = ?";
            String storedPassword = jdbc.queryForObject(sql, String.class, userId);

            if (storedPassword == null || !storedPassword.equals(oldPassword)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Old password is incorrect"
                ));
            }

            // Update password (in production, hash with bcrypt)
            jdbc.update("UPDATE users SET password_hash = ? WHERE user_id = ?", newPassword, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password changed successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error changing password"
            ));
        }
    }
}