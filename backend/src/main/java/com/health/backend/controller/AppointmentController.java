package com.health.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final JdbcTemplate jdbc;

    public AppointmentController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        String sql = """
            SELECT a.appointment_id, a.appointment_date, a.appointment_time,
                   a.status_code, a.reason, a.created_at,
                   p.patient_id, u_p.first_name AS patient_first, u_p.last_name AS patient_last,
                   d.doctor_id, u_d.first_name AS doctor_first, u_d.last_name AS doctor_last
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u_p ON p.user_id = u_p.user_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            JOIN users u_d ON d.user_id = u_d.user_id
            ORDER BY a.appointment_date DESC, a.appointment_time DESC
        """;
        return jdbc.queryForList(sql);
    }

    @GetMapping("/upcoming")
    public List<Map<String, Object>> getUpcoming() {
        String sql = """
            SELECT a.appointment_id, a.appointment_date, a.appointment_time,
                   a.status_code, a.reason,
                   u_p.first_name AS patient_first, u_p.last_name AS patient_last,
                   u_d.first_name AS doctor_first, u_d.last_name AS doctor_last
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u_p ON p.user_id = u_p.user_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            JOIN users u_d ON d.user_id = u_d.user_id
            WHERE a.appointment_date >= CURDATE() AND a.status_code != 'CANCELLED'
            ORDER BY a.appointment_date, a.appointment_time
        """;
        return jdbc.queryForList(sql);
    }

    @GetMapping("/patient/{patientId}")
    public List<Map<String, Object>> getByPatient(@PathVariable int patientId) {
        String sql = """
            SELECT a.appointment_id, a.appointment_date, a.appointment_time,
                   a.status_code, a.reason,
                   u_d.first_name AS doctor_first, u_d.last_name AS doctor_last
            FROM appointments a
            JOIN doctors d ON a.doctor_id = d.doctor_id
            JOIN users u_d ON d.user_id = u_d.user_id
            WHERE a.patient_id = ?
            ORDER BY a.appointment_date DESC, a.appointment_time DESC
        """;
        return jdbc.queryForList(sql, patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Map<String, Object>> getByDoctor(@PathVariable int doctorId) {
        String sql = """
            SELECT a.appointment_id, a.appointment_date, a.appointment_time,
                   a.status_code, a.reason,
                   u_p.first_name AS patient_first, u_p.last_name AS patient_last,
                   p.patient_id
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u_p ON p.user_id = u_p.user_id
            WHERE a.doctor_id = ?
            ORDER BY a.appointment_date, a.appointment_time
        """;
        return jdbc.queryForList(sql, doctorId);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> book(@RequestBody Map<String, Object> request) {
        int patientId = ((Number) request.get("patientId")).intValue();
        int doctorId = ((Number) request.get("doctorId")).intValue();
        String date = (String) request.get("appointmentDate");
        String time = (String) request.get("appointmentTime");
        String reason = (String) request.getOrDefault("reason", "");

        String conflictSql = """
            SELECT COUNT(*) FROM appointments
            WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ?
            AND status_code NOT IN ('CANCELLED')
        """;
        int conflicts = jdbc.queryForObject(conflictSql, Integer.class, doctorId, date, time);

        if (conflicts > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Time slot already booked"
            ));
        }

        jdbc.update("INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason) VALUES (?, ?, ?, ?, ?)",
                patientId, doctorId, date, time, reason);

        int id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        return ResponseEntity.ok(Map.of("success", true, "message", "Appointment booked", "appointmentId", id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable int id) {
        String getInfo = """
            SELECT a.patient_id, a.appointment_date, a.appointment_time,
                   u_d.first_name AS doctor_first, u_d.last_name AS doctor_last
            FROM appointments a
            JOIN doctors d ON a.doctor_id = d.doctor_id
            JOIN users u_d ON d.user_id = u_d.user_id
            WHERE a.appointment_id = ?
        """;
        Map<String, Object> info = jdbc.queryForMap(getInfo, id);

        jdbc.update("UPDATE appointments SET status_code = 'CANCELLED' WHERE appointment_id = ?", id);

        int patientId = ((Number) info.get("patient_id")).intValue();
        int patientUserId = jdbc.queryForObject(
                "SELECT user_id FROM patients WHERE patient_id = ?", Integer.class, patientId);

        String title = "Appointment Cancelled";
        String message = "Your appointment with Dr. " + info.get("doctor_first") + " " + info.get("doctor_last") +
                " on " + info.get("appointment_date") + " at " + info.get("appointment_time") + " has been cancelled.";

        jdbc.update("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)", patientUserId, title, message);

        return ResponseEntity.ok(Map.of("success", true, "message", "Appointment cancelled"));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@PathVariable int id) {
        String getInfo = """
            SELECT a.patient_id, a.appointment_date, a.appointment_time,
                   u_d.first_name AS doctor_first, u_d.last_name AS doctor_last
            FROM appointments a
            JOIN doctors d ON a.doctor_id = d.doctor_id
            JOIN users u_d ON d.user_id = u_d.user_id
            WHERE a.appointment_id = ?
        """;
        Map<String, Object> info = jdbc.queryForMap(getInfo, id);

        jdbc.update("UPDATE appointments SET status_code = 'CONFIRMED' WHERE appointment_id = ?", id);

        int patientId = ((Number) info.get("patient_id")).intValue();
        int patientUserId = jdbc.queryForObject(
                "SELECT user_id FROM patients WHERE patient_id = ?", Integer.class, patientId);

        String title = "Appointment Confirmed";
        String message = "Your appointment with Dr. " + info.get("doctor_first") + " " + info.get("doctor_last") +
                " on " + info.get("appointment_date") + " at " + info.get("appointment_time") + " has been confirmed.";

        jdbc.update("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)", patientUserId, title, message);

        return ResponseEntity.ok(Map.of("success", true, "message", "Appointment confirmed"));
    }
}