package com.health.ui.controllers.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.health.ui.app.MainApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RegisterController {

    @FXML private StackPane root;
    @FXML private ImageView bgImage;

    // Common fields
    @FXML private TextField usernameField, emailField, firstNameField, lastNameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel, successLabel;
    @FXML private Button registerBtn;

    // Role tabs
    @FXML private ToggleButton patientTab, doctorTab, adminTab;

    // Patient fields
    @FXML private VBox patientFields;
    @FXML private TextField dobField, addressField;
    @FXML private ComboBox<String> bloodCombo;

    // Doctor fields
    @FXML private VBox doctorFields;
    @FXML private TextField licenseField, expField, clinicField, specField;

    // Admin fields
    @FXML private VBox adminFields;
    @FXML private TextField deptField, accessField;

    private final Gson gson = new Gson();

    @FXML
    private void initialize() {
        bgImage.fitWidthProperty().bind(root.widthProperty());
        bgImage.fitHeightProperty().bind(root.heightProperty());

        genderCombo.getItems().addAll("Male", "Female", "Other");
        bloodCombo.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

        showPatientFields();
    }

    @FXML
    private void onPatientTab() { showPatientFields(); }

    @FXML
    private void onDoctorTab() {
        hideAllRoleFields();
        doctorFields.setVisible(true);
        doctorFields.setManaged(true);
    }

    @FXML
    private void onAdminTab() {
        hideAllRoleFields();
        adminFields.setVisible(true);
        adminFields.setManaged(true);
    }

    private void showPatientFields() {
        hideAllRoleFields();
        patientFields.setVisible(true);
        patientFields.setManaged(true);
    }

    private void hideAllRoleFields() {
        patientFields.setVisible(false);
        patientFields.setManaged(false);
        doctorFields.setVisible(false);
        doctorFields.setManaged(false);
        adminFields.setVisible(false);
        adminFields.setManaged(false);
    }

    private int getSelectedRole() {
        if (patientTab.isSelected()) return 1;
        if (doctorTab.isSelected()) return 2;
        return 3;
    }

    @FXML
    private void onRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String gender = genderCombo.getValue();
        String phone = phoneField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() ||
                firstName.isEmpty() || lastName.isEmpty()) {
            showError("Please fill all required fields.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters.");
            return;
        }

        hideMessages();
        setLoading(true);

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("username", username);
                body.addProperty("email", email);
                body.addProperty("password", password);
                body.addProperty("firstName", firstName);
                body.addProperty("lastName", lastName);
                body.addProperty("genderCode", gender != null ? gender.substring(0, 1).toUpperCase() : "O");
                body.addProperty("roleId", getSelectedRole());
                body.addProperty("phone", phone);

                if (getSelectedRole() == 1) {
                    body.addProperty("dateOfBirth", dobField.getText().trim().isEmpty() ? "2000-01-01" : dobField.getText().trim());
                    body.addProperty("bloodCode", bloodCombo.getValue() != null ? bloodCombo.getValue() : "O+");
                    body.addProperty("address", addressField.getText().trim());
                }

                if (getSelectedRole() == 2) {
                    body.addProperty("licenseNumber", licenseField.getText().trim());
                    body.addProperty("yearsOfExperience", Integer.parseInt(expField.getText().trim().isEmpty() ? "0" : expField.getText().trim()));
                    body.addProperty("clinicAddress", clinicField.getText().trim());
                    body.addProperty("specializations", specField.getText().trim());
                }

                if (getSelectedRole() == 3) {
                    body.addProperty("department", deptField.getText().trim());
                    body.addProperty("accessLevel", Integer.parseInt(accessField.getText().trim().isEmpty() ? "1" : accessField.getText().trim()));
                }

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                        body.toString(), okhttp3.MediaType.get("application/json"));
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://localhost:8080/api/auth/register")
                        .post(requestBody)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                Platform.runLater(() -> {
                    setLoading(false);
                    if (response.isSuccessful()) {
                        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                        if (json.get("success").getAsBoolean()) {
                            showSuccess("Account created! Redirecting to login...");
                            new Thread(() -> {
                                try { Thread.sleep(2000); } catch (Exception ignored) {}
                                Platform.runLater(() -> MainApp.navigateTo("/fxml/auth/Login.fxml"));
                            }).start();
                        } else {
                            showError(json.get("message").getAsString());
                        }
                    } else {
                        showError("Registration failed. Try again.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Cannot connect to server on port 8080.");
                });
            }
        }).start();
    }

    @FXML
    private void goToLogin() {
        MainApp.navigateTo("/fxml/auth/Login.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void showSuccess(String msg) {
        successLabel.setText(msg);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void setLoading(boolean loading) {
        registerBtn.setDisable(loading);
        registerBtn.setText(loading ? "Creating Account..." : "Create Account");
    }
}