package com.health.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    private final JdbcTemplate jdbc;

    public HealthRecordController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/patient/{patientId}")
    public List<Map<String, Object>> getByPatient(@PathVariable int patientId) {
        String sql = """
            SELECT hr.record_id, hr.diagnosis, hr.prescription, hr.notes, hr.record_date,
                   u.first_name AS doctor_first, u.last_name AS doctor_last
            FROM health_records hr
            JOIN doctors d ON hr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE hr.patient_id = ?
            ORDER BY hr.record_date DESC
        """;
        return jdbc.queryForList(sql, patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Map<String, Object>> getByDoctor(@PathVariable int doctorId) {
        String sql = """
            SELECT hr.record_id, hr.record_date, hr.diagnosis, hr.prescription, hr.notes,
                   p.patient_id, u.first_name AS patient_first, u.last_name AS patient_last
            FROM health_records hr
            JOIN patients p ON hr.patient_id = p.patient_id
            JOIN users u ON p.user_id = u.user_id
            WHERE hr.doctor_id = ?
            ORDER BY hr.record_date DESC
        """;
        return jdbc.queryForList(sql, doctorId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        try {
            int patientId = ((Number) request.get("patientId")).intValue();
            int doctorId = ((Number) request.get("doctorId")).intValue();
            String diagnosis = (String) request.get("diagnosis");
            String prescription = (String) request.getOrDefault("prescription", "");
            String notes = (String) request.getOrDefault("notes", "");

            String sql = """
                INSERT INTO health_records (patient_id, doctor_id, diagnosis, prescription, notes)
                VALUES (?, ?, ?, ?, ?)
            """;
            jdbc.update(sql, patientId, doctorId, diagnosis, prescription, notes);

            int id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

            // Send notification to patient
            String getUserSql = "SELECT u.user_id FROM users u JOIN patients p ON u.user_id = p.user_id WHERE p.patient_id = ?";
            int patientUserId = jdbc.queryForObject(getUserSql, Integer.class, patientId);

            String notifSql = "INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)";
            jdbc.update(notifSql, patientUserId, "New Health Record Added",
                    "A new health record has been added to your profile: " + diagnosis);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Health record added",
                    "recordId", id
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error adding health record: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/patient/{patientId}/vitals")
    public List<Map<String, Object>> getVitals(@PathVariable int patientId) {
        String sql = """
            SELECT v.*, u.first_name AS recorded_by_name
            FROM vitals v
            JOIN users u ON v.recorded_by = u.user_id
            WHERE v.patient_id = ?
            ORDER BY v.recorded_at DESC
        """;
        return jdbc.queryForList(sql, patientId);
    }
}