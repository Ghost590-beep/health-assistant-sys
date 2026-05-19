package com.health.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final JdbcTemplate jdbc;

    public DoctorController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.first_name, u.last_name,
                   d.doctor_id, d.license_number, d.years_experience, d.clinic_address, d.specialization
            FROM users u
            JOIN doctors d ON u.user_id = d.user_id
            WHERE u.role_id = 2
            ORDER BY u.first_name
        """;
        return jdbc.queryForList(sql);
    }

    @GetMapping("/{doctorId}")
    public Map<String, Object> getById(@PathVariable int doctorId) {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.first_name, u.last_name,
                   d.doctor_id, d.license_number, d.years_experience, d.clinic_address, d.specialization
            FROM users u
            JOIN doctors d ON u.user_id = d.user_id
            WHERE d.doctor_id = ?
        """;
        return jdbc.queryForMap(sql, doctorId);
    }

    // NEW: Get all patients assigned to this doctor
    @GetMapping("/{doctorId}/patients")
    public List<Map<String, Object>> getDoctorPatients(@PathVariable int doctorId) {
        String sql = """
            SELECT DISTINCT u.user_id, u.first_name, u.last_name, u.email,
                   p.patient_id, p.date_of_birth, p.blood_code, p.address,
                   COALESCE(c.contact_value, '') AS phone,
                   (SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND patient_id = p.patient_id) as appointment_count
            FROM users u
            JOIN patients p ON u.user_id = p.user_id
            JOIN appointments a ON p.patient_id = a.patient_id AND a.doctor_id = ?
            LEFT JOIN contacts c ON u.user_id = c.user_id AND c.contact_type = 'PHONE' AND c.is_primary = TRUE
            ORDER BY u.first_name
        """;
        return jdbc.queryForList(sql, doctorId, doctorId);
    }
    // NEW: Get patient history (records + vitals)
    @GetMapping("/patients/{patientId}/history")
    public Map<String, Object> getPatientHistory(@PathVariable int patientId) {
        // Get patient info
        String patientSql = """
            SELECT u.user_id, u.first_name, u.last_name, u.email,
                   p.patient_id, p.date_of_birth, p.blood_code, p.address
            FROM users u
            JOIN patients p ON u.user_id = p.user_id
            WHERE p.patient_id = ?
        """;
        Map<String, Object> patient = jdbc.queryForMap(patientSql, patientId);

        // Get health records
        String recordsSql = """
            SELECT hr.record_id, hr.record_date, hr.diagnosis, hr.prescription, hr.notes,
                   u.first_name as doctor_first, u.last_name as doctor_last
            FROM health_records hr
            JOIN doctors d ON hr.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE hr.patient_id = ?
            ORDER BY hr.record_date DESC
        """;
        List<Map<String, Object>> records = jdbc.queryForList(recordsSql, patientId);

        // Get vitals
        String vitalsSql = """
            SELECT v.vital_id, v.recorded_at, v.temperature_celsius, v.weight_kg,
                   v.height_cm, v.blood_pressure_systolic, v.blood_pressure_diastolic,
                   v.heart_rate_bpm, v.respiratory_rate, v.oxygen_saturation,
                   u.first_name as recorded_by_name
            FROM vitals v
            JOIN users u ON v.recorded_by = u.user_id
            WHERE v.patient_id = ?
            ORDER BY v.recorded_at DESC
        """;
        List<Map<String, Object>> vitals = jdbc.queryForList(vitalsSql, patientId);

        Map<String, Object> history = new java.util.HashMap<>();
        history.put("patient", patient);
        history.put("health_records", records);
        history.put("vitals", vitals);
        return history;
    }

    @GetMapping("/{doctorId}/stats")
    public Map<String, Object> getDoctorStats(@PathVariable int doctorId) {
        // Today's appointments
        String todaySql = """
            SELECT COUNT(*) FROM appointments
            WHERE doctor_id = ? AND appointment_date = CURDATE() AND status_code != 'CANCELLED'
        """;
        int todayCount = jdbc.queryForObject(todaySql, Integer.class, doctorId);

        // Total unique patients
        String patientSql = """
            SELECT COUNT(DISTINCT patient_id) FROM appointments
            WHERE doctor_id = ?
        """;
        int patientCount = jdbc.queryForObject(patientSql, Integer.class, doctorId);

        // Pending appointments
        String pendingSql = """
            SELECT COUNT(*) FROM appointments
            WHERE doctor_id = ? AND status_code = 'PENDING'
        """;
        int pendingCount = jdbc.queryForObject(pendingSql, Integer.class, doctorId);

        return Map.of(
                "todayCount", todayCount,
                "patientCount", patientCount,
                "pendingCount", pendingCount
        );
    }


}