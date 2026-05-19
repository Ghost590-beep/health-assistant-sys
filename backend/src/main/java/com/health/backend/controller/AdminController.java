package com.health.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final JdbcTemplate jdbc;

    public AdminController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        int patients = jdbc.queryForObject("SELECT COUNT(*) FROM patients", Integer.class);
        int doctors = jdbc.queryForObject("SELECT COUNT(*) FROM doctors", Integer.class);
        int appointments = jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Integer.class);
        int pending = jdbc.queryForObject("SELECT COUNT(*) FROM appointments WHERE status_code = 'PENDING'", Integer.class);

        return Map.of("patients", patients, "doctors", doctors, "appointments", appointments, "pending", pending);
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.first_name, u.last_name,
                   r.role_name, u.gender_code, u.created_at
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            ORDER BY u.created_at DESC
        """;
        return jdbc.queryForList(sql);
    }

    @GetMapping("/appointments")
    public List<Map<String, Object>> getAllAppointments() {
        String sql = """
            SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.status_code, a.reason,
                   u_p.first_name AS patient_first, u_p.last_name AS patient_last,
                   u_d.first_name AS doctor_first, u_d.last_name AS doctor_last
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u_p ON p.user_id = u_p.user_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            JOIN users u_d ON d.user_id = u_d.user_id
            ORDER BY a.appointment_date DESC
        """;
        return jdbc.queryForList(sql);
    }

    @GetMapping("/doctors")
    public List<Map<String, Object>> getAllDoctors() {
        String sql = """
            SELECT d.doctor_id, u.first_name, u.last_name, u.email,
                   d.license_number, d.years_of_experience, d.clinic_address,
                   GROUP_CONCAT(s.specialization_name SEPARATOR ', ') AS specializations
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            LEFT JOIN doctor_specializations ds ON d.doctor_id = ds.doctor_id
            LEFT JOIN specializations s ON ds.specialization_id = s.specialization_id
            GROUP BY d.doctor_id
            ORDER BY u.first_name
        """;
        return jdbc.queryForList(sql);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        jdbc.update("DELETE FROM users WHERE user_id = ?", userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "User deleted"));
    }
}