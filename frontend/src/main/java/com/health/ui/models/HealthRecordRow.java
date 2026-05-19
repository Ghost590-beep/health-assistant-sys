package com.health.ui.models;

public class HealthRecordRow {
    private String date;
    private String doctor;
    private String diagnosis;
    private String prescription;

    public HealthRecordRow(String date, String doctor, String diagnosis, String prescription) {
        this.date = date;
        this.doctor = doctor;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public String getDate() { return date; }
    public String getDoctor() { return doctor; }
    public String getDiagnosis() { return diagnosis; }
    public String getPrescription() { return prescription; }
}