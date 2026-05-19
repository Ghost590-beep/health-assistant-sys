package com.health.ui.controllers.doctor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.health.ui.app.MainApp;
import com.health.ui.utils.SessionManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import okhttp3.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DoctorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label doctorNameLabel;
    @FXML private Label specializationLabel;

    @FXML private Label sidebarUserName;
    @FXML private Label sidebarUserRole;

    @FXML private Label todayCount;
    @FXML private Label patientCount;
    @FXML private Label pendingCount;

    @FXML private TableView<Map<String, Object>> appointmentsTable;
    @FXML private TableView<Map<String, Object>> patientsTable;
    @FXML private TableView<Map<String, Object>> recordsTable;

    @FXML private ListView<String> notificationsList;

    @FXML private VBox dashboardContent;
    @FXML private ScrollPane mainScrollPane;

    @FXML private VBox appointmentsSection;
    @FXML private VBox patientsSection;
    @FXML private VBox recordsSection;
    @FXML private VBox profileSection;
    @FXML private VBox notificationsSection;

    @FXML private Label profileLicense;
    @FXML private Label profileExperience;
    @FXML private Label profileClinic;
    @FXML private Label profileSpecialization;

    @FXML private Button navDashboard;
    @FXML private Button navAppointments;
    @FXML private Button navPatients;
    @FXML private Button navRecords;
    @FXML private Button navProfile;

    @FXML private TextField searchAppointmentField;

    private List<Button> allNavItems;
    private int currentDoctorId;

    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();

    @FXML
    private void initialize() {
        String fullName = SessionManager.getInstance().getUserFullName();
        currentDoctorId = SessionManager.getInstance().getUserId();

        welcomeLabel.setText("Welcome, Dr. " + fullName);
        doctorNameLabel.setText("Dr. " + fullName);
        sidebarUserName.setText(fullName);
        sidebarUserRole.setText("Doctor");

        allNavItems = List.of(navDashboard, navAppointments, navPatients, navRecords, navProfile);

        setupTableColumns();
        loadProfile();
        loadAppointments();
        loadDoctorPatients();
        loadHealthRecords();
        loadNotifications();
    }

    // ========================== PROFILE ==========================
    private void loadProfile() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/doctors/" + currentDoctorId)
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                Map<String, Object> data = gson.fromJson(body, Map.class);

                Platform.runLater(() -> {
                    profileLicense.setText(data.get("license_number") != null ? data.get("license_number").toString() : "N/A");
                    profileExperience.setText(data.get("years_experience") != null ? data.get("years_experience") + " years" : "N/A");
                    profileClinic.setText(data.get("clinic_address") != null ? data.get("clinic_address").toString() : "N/A");
                    profileSpecialization.setText(data.get("specialization") != null ? data.get("specialization").toString() : "N/A");
                    specializationLabel.setText(data.get("specialization") != null ? data.get("specialization").toString() : "");
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ========================== APPOINTMENTS ==========================
    private void loadAppointments() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/appointments/doctor/" + currentDoctorId)
                        .build();
                Response response = client.newCall(request).execute();
                List<Map<String, Object>> list = gson.fromJson(response.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());

                Platform.runLater(() -> {
                    appointmentsTable.getItems().setAll(list);
                    String today = LocalDate.now().toString();
                    todayCount.setText(String.valueOf(list.stream()
                            .filter(a -> today.equals(a.get("appointment_date"))
                                    && !"CANCELLED".equals(a.get("status_code")))
                            .count()));
                    pendingCount.setText(String.valueOf(list.stream()
                            .filter(a -> "PENDING".equals(a.get("status_code")))
                            .count()));
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void onSearchAppointments() {
        String query = searchAppointmentField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadAppointments();
            return;
        }

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/appointments/doctor/" + currentDoctorId)
                        .build();
                Response response = client.newCall(request).execute();
                List<Map<String, Object>> list = gson.fromJson(response.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());

                List<Map<String, Object>> filtered = list.stream()
                        .filter(a -> {
                            String patient = (a.get("patient_first") + " " + a.get("patient_last")).toLowerCase();
                            String date = String.valueOf(a.get("appointment_date"));
                            String reason = String.valueOf(a.getOrDefault("reason", "")).toLowerCase();
                            return patient.contains(query) || date.contains(query) || reason.contains(query);
                        })
                        .toList();

                Platform.runLater(() -> appointmentsTable.getItems().setAll(filtered));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ========================== CONFIRM / CANCEL ==========================
    @FXML
    private void onConfirmAppointment() {
        Map<String, Object> selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an appointment first.", Alert.AlertType.WARNING);
            return;
        }
        int appointmentId = ((Number) selected.get("appointment_id")).intValue();
        updateAppointment(appointmentId, "confirm");
    }

    @FXML
    private void onCancelAppointment() {
        Map<String, Object> selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an appointment first.", Alert.AlertType.WARNING);
            return;
        }
        int appointmentId = ((Number) selected.get("appointment_id")).intValue();
        updateAppointment(appointmentId, "cancel");
    }

    private void updateAppointment(int appointmentId, String action) {
        new Thread(() -> {
            try {
                RequestBody emptyBody = RequestBody.create("", MediaType.get("application/json"));
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/appointments/" + appointmentId + "/" + action)
                        .patch(emptyBody)
                        .build();
                Response response = client.newCall(request).execute();

                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        loadAppointments();
                        loadNotifications();
                        showAlert("Appointment " + action + "ed! Patient notified.", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Failed to " + action + " appointment.", Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    // ========================== ADD HEALTH RECORD ==========================
    @FXML
    private void onAddHealthRecord() {
        Map<String, Object> selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an appointment", Alert.AlertType.WARNING);
            return;
        }
        int patientId = ((Number) selected.get("patient_id")).intValue();
        String patientName = selected.get("patient_first") + " " + selected.get("patient_last");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Health Record");
        dialog.setHeaderText("Add new record for " + patientName);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Enter diagnosis");
        diagnosisArea.setPrefRowCount(3);

        TextArea prescriptionArea = new TextArea();
        prescriptionArea.setPromptText("Enter prescription");
        prescriptionArea.setPrefRowCount(3);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter notes");
        notesArea.setPrefRowCount(3);

        content.getChildren().addAll(new Label("Diagnosis:"), diagnosisArea,
                new Label("Prescription:"), prescriptionArea,
                new Label("Notes:"), notesArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #0E121B;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                saveHealthRecord(patientId, diagnosisArea.getText(), prescriptionArea.getText(), notesArea.getText());
            }
        });
    }

    private void saveHealthRecord(int patientId, String diagnosis, String prescription, String notes) {
        new Thread(() -> {
            try {
                com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                body.addProperty("patientId", patientId);
                body.addProperty("doctorId", currentDoctorId);
                body.addProperty("diagnosis", diagnosis);
                body.addProperty("prescription", prescription);
                body.addProperty("notes", notes);

                RequestBody requestBody = RequestBody.create(body.toString(), MediaType.get("application/json"));
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/health-records")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();

                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        loadHealthRecords();
                        showAlert("Health record added!", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Failed to add health record", Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    // ========================== PATIENTS ==========================
    private void loadDoctorPatients() {
        new Thread(() -> {
            try {
                Request patientsRequest = new Request.Builder()
                        .url("http://localhost:8080/api/doctors/" + currentDoctorId + "/patients")
                        .build();
                Response patientsResponse = client.newCall(patientsRequest).execute();
                List<Map<String, Object>> list = gson.fromJson(patientsResponse.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());

                Request statsRequest = new Request.Builder()
                        .url("http://localhost:8080/api/doctors/" + currentDoctorId + "/stats")
                        .build();
                Response statsResponse = client.newCall(statsRequest).execute();
                Map<String, Object> stats = gson.fromJson(statsResponse.body().string(), Map.class);

                Platform.runLater(() -> {
                    patientsTable.getItems().setAll(list);
                    patientCount.setText(String.valueOf(((Number) stats.get("patientCount")).intValue()));
                    todayCount.setText(String.valueOf(((Number) stats.get("todayCount")).intValue()));
                    pendingCount.setText(String.valueOf(((Number) stats.get("pendingCount")).intValue()));
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void onViewPatientHistory() {
        Map<String, Object> selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a patient first.", Alert.AlertType.WARNING);
            return;
        }
        int patientId = ((Number) selected.get("patient_id")).intValue();
        String patientName = selected.get("first_name") + " " + selected.get("last_name");
        showPatientHistoryDialog(patientId, patientName);
    }

    // ========================== BOOK FOR PATIENT ==========================
    @FXML
    private void onBookForPatient() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book Appointment for Patient");
        dialog.setHeaderText("Schedule an appointment on behalf of a patient");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<String> patientCombo = new ComboBox<>();
        patientCombo.setPromptText("Select patient");
        patientCombo.setPrefWidth(300);

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/doctors/" + currentDoctorId + "/patients")
                        .build();
                Response response = client.newCall(request).execute();
                List<Map<String, Object>> patients = gson.fromJson(response.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());

                Platform.runLater(() -> {
                    for (Map<String, Object> p : patients) {
                        patientCombo.getItems().add(
                                p.get("first_name") + " " + p.get("last_name") +
                                        " | " + p.get("patient_id"));
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setPrefWidth(300);

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("09:00", "09:30", "10:00", "10:30", "11:00",
                "11:30", "14:00", "14:30", "15:00", "15:30", "16:00");
        timeCombo.setPromptText("Select time");
        timeCombo.setPrefWidth(300);

        TextField reasonField = new TextField();
        reasonField.setPromptText("Reason for visit");
        reasonField.setPrefWidth(300);

        content.getChildren().addAll(
                new Label("Patient:"), patientCombo,
                new Label("Date:"), datePicker,
                new Label("Time:"), timeCombo,
                new Label("Reason:"), reasonField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #12121A;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK && patientCombo.getValue() != null
                    && datePicker.getValue() != null && timeCombo.getValue() != null) {
                String selected = patientCombo.getValue();
                int patientId = Integer.parseInt(selected.replaceAll(".*\\| (\\d+)", "$1"));

                new Thread(() -> {
                    try {
                        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                        body.addProperty("patientId", patientId);
                        body.addProperty("doctorId", currentDoctorId);
                        body.addProperty("appointmentDate", datePicker.getValue().toString());
                        body.addProperty("appointmentTime", timeCombo.getValue() + ":00");
                        body.addProperty("reason", reasonField.getText().isEmpty() ? "Doctor consultation" : reasonField.getText());

                        Request request = new Request.Builder()
                                .url("http://localhost:8080/api/appointments")
                                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                                .build();
                        Response response = client.newCall(request).execute();
                        String respBody = response.body().string();
                        Map<String, Object> res = gson.fromJson(respBody, Map.class);

                        Platform.runLater(() -> {
                            if (Boolean.TRUE.equals(res.get("success"))) {
                                loadAppointments();
                                showAlert("Appointment booked for patient!", Alert.AlertType.INFORMATION);
                            } else {
                                showAlert(res.get("message").toString(), Alert.AlertType.WARNING);
                            }
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }).start();
            }
        });
    }

    // ========================== EDIT PROFILE ==========================
    @FXML
    private void onEditProfile() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your information");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField nameField = new TextField(SessionManager.getInstance().getUserFullName());
        nameField.setPrefWidth(300);

        TextField emailField = new TextField();
        emailField.setPrefWidth(300);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");
        phoneField.setPrefWidth(300);

        TextField clinicField = new TextField(profileClinic.getText());
        clinicField.setPromptText("Clinic address");
        clinicField.setPrefWidth(300);

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/doctors/" + currentDoctorId)
                        .build();
                Response response = client.newCall(request).execute();
                Map<String, Object> data = gson.fromJson(response.body().string(), Map.class);
                Platform.runLater(() -> {
                    emailField.setText(data.get("email") != null ? data.get("email").toString() : "");
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        content.getChildren().addAll(
                new Label("Full Name:"), nameField,
                new Label("Email:"), emailField,
                new Label("Phone:"), phoneField,
                new Label("Clinic Address:"), clinicField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #12121A;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        String[] names = nameField.getText().split(" ", 2);
                        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                        body.addProperty("firstName", names[0]);
                        body.addProperty("lastName", names.length > 1 ? names[1] : "");
                        body.addProperty("email", emailField.getText());
                        body.addProperty("phone", phoneField.getText());
                        body.addProperty("clinicAddress", clinicField.getText());

                        Request request = new Request.Builder()
                                .url("http://localhost:8080/api/patients/" + currentDoctorId)
                                .put(RequestBody.create(body.toString(), MediaType.get("application/json")))
                                .build();
                        client.newCall(request).execute();

                        Platform.runLater(() -> {
                            loadProfile();
                            showAlert("Profile updated!", Alert.AlertType.INFORMATION);
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }).start();
            }
        });
    }

    // ========================== HEALTH RECORDS ==========================
    private void loadHealthRecords() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/health-records/doctor/" + currentDoctorId)
                        .build();
                Response response = client.newCall(request).execute();
                List<Map<String, Object>> list = gson.fromJson(response.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> recordsTable.getItems().setAll(list));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ========================== NOTIFICATIONS ==========================
    private void loadNotifications() {
        new Thread(() -> {
            try {
                int userId = SessionManager.getInstance().getUserId();
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/notifications/user/" + userId)
                        .build();
                Response response = client.newCall(request).execute();
                List<Map<String, Object>> list = gson.fromJson(response.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> {
                    notificationsList.getItems().clear();
                    for (Map<String, Object> n : list) {
                        String prefix = Boolean.TRUE.equals(n.get("is_read")) ? "  " : "● ";
                        notificationsList.getItems().add(prefix + n.get("title") + " -- " + n.get("message"));
                    }
                    if (list.isEmpty()) notificationsList.getItems().add("No notifications.");
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void onSendNotification() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Send Notification");
        dialog.setHeaderText("Send a message to a patient");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<String> patientCombo = new ComboBox<>();
        patientCombo.setPromptText("Select patient");
        patientCombo.setPrefWidth(300);

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/appointments/doctor/" + currentDoctorId)
                        .build();
                Response response = client.newCall(request).execute();
                List<Map<String, Object>> appointments = gson.fromJson(response.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());

                Map<Integer, String> uniquePatients = new java.util.LinkedHashMap<>();
                for (Map<String, Object> a : appointments) {
                    int pid = ((Number) a.get("patient_id")).intValue();
                    String name = a.get("patient_first") + " " + a.get("patient_last");
                    uniquePatients.putIfAbsent(pid, name + " (ID: " + pid + ")");
                }
                Platform.runLater(() -> patientCombo.getItems().addAll(uniquePatients.values()));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        TextField titleField = new TextField();
        titleField.setPromptText("Notification title");
        TextArea messageField = new TextArea();
        messageField.setPromptText("Type your message...");
        messageField.setPrefRowCount(4);

        content.getChildren().addAll(new Label("Patient:"), patientCombo,
                new Label("Title:"), titleField,
                new Label("Message:"), messageField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #0E121B;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK && patientCombo.getValue() != null
                    && !titleField.getText().isEmpty() && !messageField.getText().isEmpty()) {
                String selected = patientCombo.getValue();
                int patientId = Integer.parseInt(selected.replaceAll(".*ID: (\\d+)\\)", "$1"));
                new Thread(() -> {
                    try {
                        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                        body.addProperty("patientId", patientId);
                        body.addProperty("title", titleField.getText());
                        body.addProperty("message", messageField.getText());
                        Request request = new Request.Builder()
                                .url("http://localhost:8080/api/notifications/send")
                                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                                .build();
                        client.newCall(request).execute();
                        Platform.runLater(() -> {
                            showAlert("Notification sent!", Alert.AlertType.INFORMATION);
                            loadNotifications();
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }).start();
            }
        });
    }

    // ========================== NAVIGATION ==========================
    @FXML private void onNavDashboard() { resetNav(); setActiveNav(navDashboard); mainScrollPane.setVvalue(0); }
    @FXML private void onNavAppointments() { resetNav(); setActiveNav(navAppointments); scrollToNode(appointmentsSection); }
    @FXML private void onNavPatients() { resetNav(); setActiveNav(navPatients); scrollToNode(patientsSection); }
    @FXML private void onNavRecords() { resetNav(); setActiveNav(navRecords); scrollToNode(recordsSection); }
    @FXML private void onNavProfile() { resetNav(); setActiveNav(navProfile); scrollToNode(profileSection); }

    private void resetNav() { for (Button b : allNavItems) b.getStyleClass().remove("nav-item-active-btn"); }
    private void setActiveNav(Button b) { b.getStyleClass().add("nav-item-active-btn"); }

    private void scrollToNode(Node node) {
        Platform.runLater(() -> {
            double h = dashboardContent.getBoundsInLocal().getHeight();
            double y = node.getBoundsInParent().getMinY();
            mainScrollPane.setVvalue(Math.min(y / h, 1.0));
        });
    }

    // ========================== TABLE COLUMNS ==========================
    private void setupTableColumns() {
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(0))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("appointment_date"))));
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(1))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("appointment_time"))));
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(2))
                .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("patient_first") + " " + d.getValue().get("patient_last")));
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(3))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("reason"))));
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(4))
                .setCellValueFactory(d -> new SimpleStringProperty(getStatusBadge(d.getValue().get("status_code"))));

        ((TableColumn<Map<String, Object>, String>) patientsTable.getColumns().get(0))
                .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("first_name") + " " + d.getValue().get("last_name")));
        ((TableColumn<Map<String, Object>, String>) patientsTable.getColumns().get(1))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("email"))));
        ((TableColumn<Map<String, Object>, String>) patientsTable.getColumns().get(2))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOrDefault("phone", "—"))));
        ((TableColumn<Map<String, Object>, String>) patientsTable.getColumns().get(3))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOrDefault("blood_code", "N/A"))));
        ((TableColumn<Map<String, Object>, String>) patientsTable.getColumns().get(4))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOrDefault("appointment_count", "0"))));

        // Records Table
        ((TableColumn<Map<String, Object>, String>) recordsTable.getColumns().get(0))
                .setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue().get("record_date") != null ? d.getValue().get("record_date").toString().substring(0, 10) : ""));
        ((TableColumn<Map<String, Object>, String>) recordsTable.getColumns().get(1))
                .setCellValueFactory(d -> new SimpleStringProperty(
                        d.getValue().get("patient_first") + " " + d.getValue().get("patient_last")));
        ((TableColumn<Map<String, Object>, String>) recordsTable.getColumns().get(2))
                .setCellValueFactory(d -> new SimpleStringProperty(
                        String.valueOf(d.getValue().getOrDefault("diagnosis", ""))));
        ((TableColumn<Map<String, Object>, String>) recordsTable.getColumns().get(3))
                .setCellValueFactory(d -> new SimpleStringProperty(
                        String.valueOf(d.getValue().getOrDefault("prescription", ""))));
    }

    private String getStatusBadge(Object s) {
        String str = s != null ? s.toString() : "PENDING";
        return switch (str.toUpperCase()) {
            case "CONFIRMED" -> "🟢 Confirmed";
            case "PENDING" -> "🟡 Pending";
            case "CANCELLED" -> "🔴 Cancelled";
            case "COMPLETED" -> "🔵 Completed";
            default -> "⚪ " + str;
        };
    }

    private void showAlert(String message, Alert.AlertType type) {
        new Alert(type, message).show();
    }

    @FXML
    private void onLogout() {
        SessionManager.getInstance().clearSession();
        MainApp.navigateTo("/fxml/auth/Login.fxml");
    }

    // ========================== PATIENT HISTORY DIALOG ==========================
    private void showPatientHistoryDialog(int patientId, String patientName) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Patient History — " + patientName);
        dialog.setWidth(800);
        dialog.setHeight(600);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab recordsTab = new Tab("📋 Health Records");
        recordsTab.setContent(createRecordsTable(patientId));
        recordsTab.setClosable(false);

        Tab vitalsTab = new Tab("🫀 Vitals History");
        vitalsTab.setContent(createVitalsTable(patientId));
        vitalsTab.setClosable(false);

        tabPane.getTabs().addAll(recordsTab, vitalsTab);
        content.getChildren().add(tabPane);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #12121A; -fx-border-color: #1E1E2E;");
        dialog.showAndWait();
    }

    private TableView<Map<String, Object>> createRecordsTable(int patientId) {
        TableView<Map<String, Object>> table = new TableView<>();

        TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("record_date") != null ? d.getValue().get("record_date").toString().substring(0, 10) : ""));

        TableColumn<Map<String, Object>, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("doctor_first") + " " + d.getValue().get("doctor_last")));

        TableColumn<Map<String, Object>, String> diagCol = new TableColumn<>("Diagnosis");
        diagCol.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getOrDefault("diagnosis", ""))));

        TableColumn<Map<String, Object>, String> prescCol = new TableColumn<>("Prescription");
        prescCol.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getOrDefault("prescription", ""))));

        table.getColumns().addAll(dateCol, doctorCol, diagCol, prescCol);
        table.setPrefHeight(350);

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/health-records/patient/" + patientId)
                        .build();
                Response response = client.newCall(request).execute();
                List<Map<String, Object>> records = gson.fromJson(response.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> table.getItems().setAll(records));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        return table;
    }

    private TableView<Map<String, Object>> createVitalsTable(int patientId) {
        TableView<Map<String, Object>> table = new TableView<>();

        TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("recorded_at") != null ? d.getValue().get("recorded_at").toString().substring(0, 10) : ""));

        TableColumn<Map<String, Object>, String> tempCol = new TableColumn<>("Temp (°C)");
        tempCol.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getOrDefault("temperature_celsius", "—"))));

        TableColumn<Map<String, Object>, String> bpCol = new TableColumn<>("Blood Pressure");
        bpCol.setCellValueFactory(d -> {
            Object sys = d.getValue().get("blood_pressure_systolic");
            Object dia = d.getValue().get("blood_pressure_diastolic");
            return new SimpleStringProperty(sys + "/" + dia);
        });

        TableColumn<Map<String, Object>, String> hrCol = new TableColumn<>("Heart Rate");
        hrCol.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getOrDefault("heart_rate_bpm", "—"))));

        TableColumn<Map<String, Object>, String> o2Col = new TableColumn<>("O2 Sat (%)");
        o2Col.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getOrDefault("oxygen_saturation", "—"))));

        table.getColumns().addAll(dateCol, tempCol, bpCol, hrCol, o2Col);
        table.setPrefHeight(350);

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/health-records/patient/" + patientId + "/vitals")
                        .build();
                Response response = client.newCall(request).execute();
                List<Map<String, Object>> vitals = gson.fromJson(response.body().string(),
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> table.getItems().setAll(vitals));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        return table;
    }
}