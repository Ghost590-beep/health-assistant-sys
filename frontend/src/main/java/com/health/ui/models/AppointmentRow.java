package com.health.ui.models;

public class AppointmentRow {
    private String date;
    private String time;
    private String doctor;
    private String reason;
    private String status;

    public AppointmentRow(String date, String time, String doctor, String reason, String status) {
        this.date = date;
        this.time = time;
        this.doctor = doctor;
        this.reason = reason;
        this.status = status;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDoctor() { return doctor; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
}