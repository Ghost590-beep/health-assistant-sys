package com.health.ui.controllers.admin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.health.ui.app.MainApp;
import com.health.ui.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import okhttp3.*;
import java.util.List;
import java.util.Map;

public class AdminDashboardController {

    @FXML private Label welcomeLabel, adminTitleLabel, sidebarUserName, sidebarUserRole;
    @FXML private Label statPatients, statDoctors, statAppointments, statPending;
    @FXML private TableView<Map<String, Object>> usersTable, appointmentsTable, doctorsTable;
    @FXML private Button navDashboard, navUsers, navAppointments, navDoctors;
    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox dashboardContent;
    @FXML private VBox usersSection, appointmentsSection, doctorsSection;

    private List<Button> allNavItems;
    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();

    @FXML
    private void initialize() {
        String fullName = SessionManager.getInstance().getUserFullName();
        welcomeLabel.setText("Welcome, " + fullName);
        adminTitleLabel.setText("Admin Dashboard");
        sidebarUserName.setText(fullName);
        sidebarUserRole.setText("Administrator");

        allNavItems = List.of(navDashboard, navUsers, navAppointments, navDoctors);

        setupColumns();
        loadStats();
        loadUsers();
        loadAppointments();
        loadDoctors();
    }

    // ========================== TABLE COLUMNS ==========================
    private void setupColumns() {
        ((TableColumn<Map<String, Object>, String>) usersTable.getColumns().get(0))
                .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("first_name") + " " + d.getValue().get("last_name")));
        ((TableColumn<Map<String, Object>, String>) usersTable.getColumns().get(1))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("email"))));
        ((TableColumn<Map<String, Object>, String>) usersTable.getColumns().get(2))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("role_name"))));
        ((TableColumn<Map<String, Object>, String>) usersTable.getColumns().get(3))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOrDefault("gender_code", ""))));

        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(0))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("appointment_date"))));
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(1))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("appointment_time"))));
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(2))
                .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("patient_first") + " " + d.getValue().get("patient_last")));
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(3))
                .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("doctor_first") + " " + d.getValue().get("doctor_last")));
        ((TableColumn<Map<String, Object>, String>) appointmentsTable.getColumns().get(4))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("status_code"))));

        ((TableColumn<Map<String, Object>, String>) doctorsTable.getColumns().get(0))
                .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("first_name") + " " + d.getValue().get("last_name")));
        ((TableColumn<Map<String, Object>, String>) doctorsTable.getColumns().get(1))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("email"))));
        ((TableColumn<Map<String, Object>, String>) doctorsTable.getColumns().get(2))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("license_number"))));
        ((TableColumn<Map<String, Object>, String>) doctorsTable.getColumns().get(3))
                .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOrDefault("specializations", ""))));
    }

    // ========================== LOAD DATA ==========================
    private void loadStats() {
        new Thread(() -> {
            try {
                Request r = new Request.Builder().url("http://localhost:8080/api/admin/stats").build();
                Response res = client.newCall(r).execute();
                Map<String, Object> stats = gson.fromJson(res.body().string(), Map.class);
                Platform.runLater(() -> {
                    statPatients.setText(String.valueOf(stats.get("patients")));
                    statDoctors.setText(String.valueOf(stats.get("doctors")));
                    statAppointments.setText(String.valueOf(stats.get("appointments")));
                    statPending.setText(String.valueOf(stats.get("pending")));
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                Request r = new Request.Builder().url("http://localhost:8080/api/admin/users").build();
                Response res = client.newCall(r).execute();
                List<Map<String, Object>> list = gson.fromJson(res.body().string(), new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> usersTable.getItems().setAll(list));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadAppointments() {
        new Thread(() -> {
            try {
                Request r = new Request.Builder().url("http://localhost:8080/api/admin/appointments").build();
                Response res = client.newCall(r).execute();
                List<Map<String, Object>> list = gson.fromJson(res.body().string(), new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> appointmentsTable.getItems().setAll(list));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadDoctors() {
        new Thread(() -> {
            try {
                Request r = new Request.Builder().url("http://localhost:8080/api/admin/doctors").build();
                Response res = client.newCall(r).execute();
                List<Map<String, Object>> list = gson.fromJson(res.body().string(), new TypeToken<List<Map<String, Object>>>(){}.getType());
                Platform.runLater(() -> doctorsTable.getItems().setAll(list));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ========================== ACTIONS ==========================
    @FXML
    private void onDeleteUser() {
        Map<String, Object> selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a user first.").show();
            return;
        }
        int userId = ((Number) selected.get("user_id")).intValue();
        new Thread(() -> {
            try {
                Request r = new Request.Builder().url("http://localhost:8080/api/admin/users/" + userId).delete().build();
                client.newCall(r).execute();
                Platform.runLater(() -> { loadUsers(); loadStats(); });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void onCancelAppointment() {
        Map<String, Object> selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select an appointment first.").show();
            return;
        }
        int id = ((Number) selected.get("appointment_id")).intValue();
        new Thread(() -> {
            try {
                RequestBody b = RequestBody.create("", MediaType.get("application/json"));
                Request r = new Request.Builder().url("http://localhost:8080/api/appointments/" + id + "/cancel").patch(b).build();
                client.newCall(r).execute();
                Platform.runLater(() -> loadAppointments());
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ========================== NAVIGATION ==========================
    @FXML private void onNavDashboard() { resetNav(); setActiveNav(navDashboard); mainScrollPane.setVvalue(0); }
    @FXML private void onNavUsers() { resetNav(); setActiveNav(navUsers); scrollTo(usersSection); }
    @FXML private void onNavAppointments() { resetNav(); setActiveNav(navAppointments); scrollTo(appointmentsSection); }
    @FXML private void onNavDoctors() { resetNav(); setActiveNav(navDoctors); scrollTo(doctorsSection); }

    private void resetNav() { for (Button b : allNavItems) b.getStyleClass().remove("nav-item-active-btn"); }
    private void setActiveNav(Button b) { b.getStyleClass().add("nav-item-active-btn"); }

    private void scrollTo(Node node) {
        Platform.runLater(() -> {
            double h = dashboardContent.getBoundsInLocal().getHeight();
            double y = node.getBoundsInParent().getMinY();
            mainScrollPane.setVvalue(Math.min(y / h, 1.0));
        });
    }

    @FXML
    private void onConfirmAppointment() {
        Map<String, Object> selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select an appointment first.").show();
            return;
        }
        int id = ((Number) selected.get("appointment_id")).intValue();
        new Thread(() -> {
            try {
                RequestBody b = RequestBody.create("", MediaType.get("application/json"));
                Request r = new Request.Builder().url("http://localhost:8080/api/appointments/" + id + "/confirm").patch(b).build();
                client.newCall(r).execute();
                Platform.runLater(() -> loadAppointments());
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
    @FXML
    private void onLogout() {
        SessionManager.getInstance().clearSession();
        MainApp.navigateTo("/fxml/auth/Login.fxml");
    }
}