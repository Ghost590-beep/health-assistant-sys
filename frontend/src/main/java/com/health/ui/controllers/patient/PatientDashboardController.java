package com.health.ui.controllers.patient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.health.ui.app.MainApp;
import com.health.ui.utils.SessionManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import okhttp3.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PatientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label patientNameLabel;

    @FXML private Label sidebarUserName;
    @FXML private Label sidebarUserRole;

    @FXML private Label appointmentCount;
    @FXML private Label recordCount;
    @FXML private Label vitalCount;

    @FXML private TableView<Map<String, Object>> appointmentsTable;
    @FXML private TableView<Map<String, Object>> recordsTable;

    @FXML private ListView<String> notificationsList;

    @FXML private VBox vitalsContainer;
    @FXML private VBox dashboardContent;

    @FXML private ScrollPane mainScrollPane;

    @FXML private VBox appointmentsSection;
    @FXML private VBox recordsSection;
    @FXML private VBox vitalsSection;
    @FXML private VBox profileSection;
    @FXML private VBox notificationsSection;

    @FXML private TextField profileNameField;
    @FXML private TextField profileEmailField;
    @FXML private TextField profilePhone;
    @FXML private TextField profileAddress;
    @FXML private PasswordField profileOldPassword;
    @FXML private PasswordField profileNewPassword;
    @FXML private PasswordField profileConfirmPassword;

    @FXML private Label profileUpdateMsg;

    @FXML private Button navDashboard;
    @FXML private Button navAppointments;
    @FXML private Button navRecords;
    @FXML private Button navVitals;
    @FXML private Button navProfile;
    @FXML private Button navNotifications;

    private List<Button> allNavItems;

    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();
    private int currentPatientId;

    @FXML
    private void initialize() {
        String fullName = SessionManager.getInstance().getUserFullName();
        welcomeLabel.setText("Welcome, " + fullName);
        patientNameLabel.setText(fullName + "'s Dashboard");
        sidebarUserName.setText(fullName);
        sidebarUserRole.setText("Patient");

        allNavItems = List.of(
                navDashboard, navAppointments, navRecords,
                navVitals, navProfile, navNotifications
        );

        setupTableColumns();
        loadProfile();
        loadAppointments();
        loadHealthRecords();
        loadVitals();
        loadNotifications();
    }

    // ========================== PROFILE ==========================
    private void loadProfile() {
        new Thread(() -> {
            try {
                int userId = SessionManager.getInstance().getUserId();

                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/patients/" + userId)
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();
                Map<String, Object> data = gson.fromJson(body, Map.class);

                Platform.runLater(() -> {
                    // Store patient_id for later use
                    Object patientIdObj = data.get("patient_id");
                    if (patientIdObj instanceof Double) {
                        currentPatientId = ((Double) patientIdObj).intValue();
                    } else {
                        currentPatientId = (int) patientIdObj;
                    }

                    String firstName = data.get("first_name") != null
                            ? data.get("first_name").toString() : "";
                    String lastName = data.get("last_name") != null
                            ? data.get("last_name").toString() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    String email = data.get("email") != null
                            ? data.get("email").toString() : "";
                    String phone = data.get("phone") != null
                            ? data.get("phone").toString() : "";
                    String address = data.get("address") != null
                            ? data.get("address").toString() : "";

                    profileNameField.setText(fullName);
                    profileEmailField.setText(email);
                    profilePhone.setText(phone);
                    profileAddress.setText(address);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onUpdateProfile() {
        String fullName = profileNameField.getText().trim();
        String email = profileEmailField.getText().trim();
        String phone = profilePhone.getText().trim();
        String address = profileAddress.getText().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            showProfileMsg("Name and email are required.", "#FF6B6B");
            return;
        }

        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        new Thread(() -> {
            try {
                int userId = SessionManager.getInstance().getUserId();

                com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                body.addProperty("firstName", firstName);
                body.addProperty("lastName", lastName);
                body.addProperty("email", email);
                body.addProperty("phone", phone);
                body.addProperty("address", address);

                RequestBody requestBody = RequestBody.create(
                        body.toString(), MediaType.get("application/json"));

                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/patients/" + userId)
                        .put(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        SessionManager.getInstance().setUserFullName(fullName);
                        welcomeLabel.setText("Welcome, " + fullName);
                        patientNameLabel.setText(fullName + "'s Dashboard");
                        sidebarUserName.setText(fullName);
                        showProfileMsg("Profile updated successfully!", "#00E6A0");
                    } else {
                        showProfileMsg("Failed to update profile.", "#FF6B6B");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> showProfileMsg("Connection error.", "#FF6B6B"));
            }
        }).start();
    }

    @FXML
    private void onChangePassword() {
        String oldPassword = profileOldPassword.getText();
        String newPassword = profileNewPassword.getText();
        String confirmPassword = profileConfirmPassword.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            showProfileMsg("All password fields are required.", "#FF6B6B");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showProfileMsg("New passwords do not match.", "#FF6B6B");
            return;
        }

        if (newPassword.length() < 6) {
            showProfileMsg("Password must be at least 6 characters.", "#FF6B6B");
            return;
        }

        new Thread(() -> {
            try {
                int userId = SessionManager.getInstance().getUserId();

                com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                body.addProperty("oldPassword", oldPassword);
                body.addProperty("newPassword", newPassword);

                RequestBody requestBody = RequestBody.create(
                        body.toString(), MediaType.get("application/json"));

                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/patients/" + userId + "/change-password")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String responseStr = response.body().string();
                Map<String, Object> result = gson.fromJson(responseStr, Map.class);

                Platform.runLater(() -> {
                    if (Boolean.TRUE.equals(result.get("success"))) {
                        showProfileMsg("Password changed successfully!", "#00E6A0");
                        profileOldPassword.clear();
                        profileNewPassword.clear();
                        profileConfirmPassword.clear();
                    } else {
                        showProfileMsg(result.get("message").toString(), "#FF6B6B");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> showProfileMsg("Error changing password.", "#FF6B6B"));
            }
        }).start();
    }

    private void showProfileMsg(String msg, String color) {
        profileUpdateMsg.setText(msg);
        profileUpdateMsg.setStyle("-fx-text-fill: " + color + "; -fx-padding: 8px;");
        profileUpdateMsg.setVisible(true);
    }

    // ========================== NAVIGATION ==========================
    @FXML private void onNavDashboard() { resetNav(); setActiveNav(navDashboard); mainScrollPane.setVvalue(0); }
    @FXML private void onNavAppointments() { resetNav(); setActiveNav(navAppointments); scrollToNode(appointmentsSection); }
    @FXML private void onNavRecords() { resetNav(); setActiveNav(navRecords); scrollToNode(recordsSection); }
    @FXML private void onNavVitals() { resetNav(); setActiveNav(navVitals); scrollToNode(vitalsSection); }
    @FXML private void onNavProfile() { resetNav(); setActiveNav(navProfile); scrollToNode(profileSection); }
    @FXML private void onNavNotifications() { resetNav(); setActiveNav(navNotifications); scrollToNode(notificationsSection); }

    private void resetNav() {
        for (Button item : allNavItems) {
            item.getStyleClass().remove("nav-item-active-btn");
        }
    }

    private void setActiveNav(Button navItem) {
        navItem.getStyleClass().add("nav-item-active-btn");
    }

    private void scrollToNode(Node node) {
        Platform.runLater(() -> {
            double height = dashboardContent.getBoundsInLocal().getHeight();
            double y = node.getBoundsInParent().getMinY();
            mainScrollPane.setVvalue(Math.min(y / height, 1.0));
        });
    }

    // ========================== BOOK APPOINTMENT ==========================
    @FXML
    private void onBookAppointment() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book Appointment");
        dialog.setHeaderText("Schedule a new appointment");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<String> doctorCombo = new ComboBox<>();
        doctorCombo.setPromptText("Select doctor");
        doctorCombo.setPrefWidth(300);

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

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/doctors")
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                List<Map<String, Object>> doctors = gson.fromJson(body,
                        new TypeToken<List<Map<String, Object>>>(){}.getType());

                Platform.runLater(() -> {
                    for (Map<String, Object> d : doctors) {
                        doctorCombo.getItems().add(
                                d.get("first_name") + " " + d.get("last_name") +
                                        " | " + d.get("doctor_id"));
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        content.getChildren().addAll(
                new Label("Doctor:"), doctorCombo,
                new Label("Date:"), datePicker,
                new Label("Time:"), timeCombo,
                new Label("Reason:"), reasonField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #0E121B;");

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK && doctorCombo.getValue() != null
                    && datePicker.getValue() != null && timeCombo.getValue() != null) {
                bookAppointment(doctorCombo.getValue(), datePicker.getValue(),
                        timeCombo.getValue(), reasonField.getText());
            }
        });
    }

    private void bookAppointment(String doctorInfo, LocalDate date, String time, String reason) {
        String[] parts = doctorInfo.split("\\|");
        if (parts.length < 2) return;

        // Parse as Double first, then convert to int to handle "1.0" format
        int doctorId = (int) Double.parseDouble(parts[1].trim());
        int patientId = SessionManager.getInstance().getUserId();

        new Thread(() -> {
            try {
                com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                body.addProperty("patientId", patientId);
                body.addProperty("doctorId", doctorId);
                body.addProperty("appointmentDate", date.toString());
                body.addProperty("appointmentTime", time + ":00");
                body.addProperty("reason", reason.isEmpty() ? "General consultation" : reason);

                RequestBody requestBody = RequestBody.create(
                        body.toString(), MediaType.get("application/json"));
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/appointments")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                Map<String, Object> result = gson.fromJson(responseBody, Map.class);

                Platform.runLater(() -> {
                    if (Boolean.TRUE.equals(result.get("success"))) {
                        loadAppointments();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment booked successfully!");
                        alert.setTitle("Success");
                        alert.show();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                result.get("message") != null ? result.get("message").toString() : "Failed to book");
                        alert.setTitle("Error");
                        alert.show();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Connection error: " + e.getMessage());
                    alert.show();
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ========================== TABLE COLUMNS ==========================
    private void setupTableColumns() {
        // APPOINTMENTS TABLE
        TableColumn<Map<String, Object>, String> aptDateCol =
                (TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(0);
        aptDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("appointment_date"))));

        TableColumn<Map<String, Object>, String> aptTimeCol =
                (TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(1);
        aptTimeCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("appointment_time"))));

        TableColumn<Map<String, Object>, String> aptDoctorCol =
                (TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(2);
        aptDoctorCol.setCellValueFactory(data -> {
            String doctor = data.getValue().get("doctor_first") + " " + data.getValue().get("doctor_last");
            return new SimpleStringProperty(doctor);
        });

        TableColumn<Map<String, Object>, String> aptReasonCol =
                (TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(3);
        aptReasonCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("reason"))));

        TableColumn<Map<String, Object>, String> aptStatusCol =
                (TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(4);
        aptStatusCol.setCellValueFactory(data ->
                new SimpleStringProperty(getStatusBadge(data.getValue().get("status_code"))));

        // RECORDS TABLE
        TableColumn<Map<String, Object>, String> recDateCol =
                (TableColumn<Map<String, Object>, String>) recordsTable.getColumns().get(0);
        recDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("record_date")).substring(0, 10)));

        TableColumn<Map<String, Object>, String> recDoctorCol =
                (TableColumn<Map<String, Object>, String>) recordsTable.getColumns().get(1);
        recDoctorCol.setCellValueFactory(data -> {
            String doctor = data.getValue().get("doctor_first") + " " + data.getValue().get("doctor_last");
            return new SimpleStringProperty(doctor);
        });

        TableColumn<Map<String, Object>, String> recDiagnosisCol =
                (TableColumn<Map<String, Object>, String>) recordsTable.getColumns().get(2);
        recDiagnosisCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("diagnosis"))));

        TableColumn<Map<String, Object>, String> recPrescriptionCol =
                (TableColumn<Map<String, Object>, String>) recordsTable.getColumns().get(3);
        recPrescriptionCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getOrDefault("prescription", "N/A"))));
    }

    private String getStatusBadge(Object status) {
        String s = status != null ? status.toString() : "PENDING";
        return switch (s.toUpperCase()) {
            case "CONFIRMED" -> "🟢 Confirmed";
            case "PENDING" -> "🟡 Pending";
            case "CANCELLED" -> "🔴 Cancelled";
            case "COMPLETED" -> "🔵 Completed";
            default -> "⚪ " + s;
        };
    }

    // ========================== APPOINTMENTS ==========================
    private void loadAppointments() {
        new Thread(() -> {
            try {
                int patientId = SessionManager.getInstance().getUserId();
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/appointments/patient/" + patientId)
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                List<Map<String, Object>> list = gson.fromJson(body,
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> {
                    appointmentCount.setText(String.valueOf(list.size()));
                    appointmentsTable.getItems().setAll(list);
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ========================== HEALTH RECORDS ==========================
    private void loadHealthRecords() {
        new Thread(() -> {
            try {
                int patientId = SessionManager.getInstance().getUserId();
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/health-records/patient/" + patientId)
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                List<Map<String, Object>> list = gson.fromJson(body,
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> {
                    recordCount.setText(String.valueOf(list.size()));
                    recordsTable.getItems().setAll(list);
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ========================== VITALS ==========================
    private void loadVitals() {
        new Thread(() -> {
            try {
                // Wait for patient_id to be loaded
                if (currentPatientId == 0) {
                    Thread.sleep(500);
                }

                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/health-records/patient/" + currentPatientId + "/vitals")
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                List<Map<String, Object>> list = gson.fromJson(body,
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> {
                    vitalCount.setText(String.valueOf(list.size()));
                    vitalsContainer.getChildren().clear();
                    if (!list.isEmpty()) {
                        Map<String, Object> latest = list.get(0);
                        vitalsContainer.getChildren().add(
                                createVitalRow("Temperature", formatValue(latest, "temperature_celsius") + " °C", "#FF6B6B", 0.6));
                        vitalsContainer.getChildren().add(
                                createVitalRow("Blood Pressure",
                                        formatValue(latest, "blood_pressure_systolic") + "/" +
                                                formatValue(latest, "blood_pressure_diastolic") + " mmHg", "#4F7FFF", 0.7));
                        vitalsContainer.getChildren().add(
                                createVitalRow("Heart Rate", formatValue(latest, "heart_rate_bpm") + " bpm", "#00C9A7", 0.5));
                        vitalsContainer.getChildren().add(
                                createVitalRow("Oxygen Saturation", formatValue(latest, "oxygen_saturation") + "%", "#FFB830", 0.9));
                        vitalsContainer.getChildren().add(
                                createVitalRow("Weight", formatValue(latest, "weight_kg") + " kg", "#C9D1D9", 0.4));
                        vitalsContainer.getChildren().add(
                                createVitalRow("Height", formatValue(latest, "height_cm") + " cm", "#C9D1D9", 0.65));
                        vitalsContainer.getChildren().add(
                                createVitalRow("Respiratory Rate", formatValue(latest, "respiratory_rate") + " /min", "#C9D1D9", 0.3));
                    } else {
                        Label empty = new Label("No vitals recorded yet.");
                        empty.setStyle("-fx-text-fill: #8B949E; -fx-font-size: 13px;");
                        vitalsContainer.getChildren().add(empty);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private String formatValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "--";
    }

    private HBox createVitalRow(String label, String value, String color, double progress) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(16);
        row.setStyle("-fx-padding: 8 0 8 0;");
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #8B949E; -fx-font-size: 13px; -fx-min-width: 140;");
        ProgressBar bar = new ProgressBar(progress);
        bar.setPrefWidth(200);
        bar.setStyle("-fx-accent: " + color + ";");
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 80;");
        row.getChildren().addAll(labelNode, bar, valueNode);
        return row;
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
                String body = response.body().string();
                List<Map<String, Object>> list = gson.fromJson(body,
                        new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> {
                    notificationsList.getItems().clear();
                    for (Map<String, Object> n : list) {
                        String prefix = Boolean.TRUE.equals(n.get("is_read")) ? "  " : "● ";
                        notificationsList.getItems().add(
                                prefix + n.get("title") + " -- " + n.get("message"));
                    }
                    if (list.isEmpty()) {
                        notificationsList.getItems().add("No notifications yet.");
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ========================== LOGOUT ==========================
    @FXML
    private void onLogout() {
        SessionManager.getInstance().clearSession();
        MainApp.navigateTo("/fxml/auth/Login.fxml");
    }
}