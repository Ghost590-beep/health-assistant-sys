package com.health.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final JdbcTemplate jdbc;

    public NotificationController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getByUser(@PathVariable int userId) {
        String sql = """
            SELECT notification_id, title, message, is_read, created_at
            FROM notifications
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT 20
        """;
        return jdbc.queryForList(sql, userId);
    }

    @PatchMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable int id) {
        jdbc.update("UPDATE notifications SET is_read = TRUE WHERE notification_id = ?", id);
        return Map.of("success", true);
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(@RequestBody Map<String, Object> request) {
        try {
            int patientId = ((Number) request.get("patientId")).intValue();
            String title = (String) request.get("title");
            String message = (String) request.get("message");

            // Get patient's user_id
            String sql = "SELECT user_id FROM patients WHERE patient_id = ?";
            int patientUserId = jdbc.queryForObject(sql, Integer.class, patientId);

            // Insert notification
            jdbc.update("INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)",
                    patientUserId, title, message);

            return ResponseEntity.ok(Map.of("success", true, "message", "Notification sent to patient"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error sending notification: " + e.getMessage()
            ));
        }
    }
}