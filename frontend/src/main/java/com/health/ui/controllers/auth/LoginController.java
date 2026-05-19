package com.health.ui.controllers.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.health.ui.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import com.health.ui.app.MainApp;

public class LoginController {

    // ── FXML injected nodes ───────────────────────────────────────────────────
    @FXML private StackPane   root;          // the root StackPane (fx:id="root")
    @FXML private ImageView   bgImage;       // background image
    @FXML private TextField   emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label        errorLabel;
    @FXML private Button       loginBtn;

    private final Gson gson = new Gson();

    /**
     * Called automatically after FXML injection.
     * We bind the ImageView dimensions to the root StackPane here
     * because FXML expression bindings like ${root.width} resolve
     * incorrectly against named child nodes, causing a NumberFormatException.
     */
    @FXML
    private void initialize() {
        // Bind image size to root pane — this is what makes bg truly responsive
        bgImage.fitWidthProperty().bind(root.widthProperty());
        bgImage.fitHeightProperty().bind(root.heightProperty());
    }

    // ── Login action ─────────────────────────────────────────────────────────
    @FXML
    private void onLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        hideError();
        setLoading(true);

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("email",    email);
                body.addProperty("password", password);

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                        body.toString(),
                        okhttp3.MediaType.get("application/json"));
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://localhost:8080/api/auth/login")
                        .post(requestBody)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                Platform.runLater(() -> {
                    setLoading(false);
                    if (response.isSuccessful()) {
                        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                        if (json.get("success").getAsBoolean()) {
                            String token    = json.get("token").getAsString();
                            String role     = json.get("role").getAsString();
                            int    userId   = json.get("userId").getAsInt();
                            String fullName = json.get("fullName").getAsString();

                            SessionManager.getInstance().setSession(token, role, userId, fullName);
                            navigateByRole(role);
                        } else {
                            showError(json.get("message").getAsString());
                        }
                    } else {
                        showError("Invalid email or password.");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Cannot connect to server. Is it running on port 8080?");
                });
            }
        }).start();
    }

    private void navigateByRole(String role) {
        switch (role) {
            case "ADMIN" -> MainApp.navigateTo("/fxml/auth/admin/AdminDashboard.fxml");
            case "DOCTOR" -> MainApp.navigateTo("/fxml/auth/doctor/DoctorDashboard.fxml");
            case "PATIENT" -> MainApp.navigateTo("/fxml/auth/patient/PatientDashboard.fxml");
            default -> System.out.println("Unknown role");
        }
    }

    // ── Other actions ────────────────────────────────────────────────────────
    @FXML
    private void goToRegister() {
        MainApp.navigateTo("/fxml/auth/Register.fxml");
    }
    @FXML private void onGoogleSignIn() { System.out.println("Google sign-in — not implemented"); }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);   // gives the label layout space
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);  // collapses space so layout doesn't shift
    }

    private void setLoading(boolean loading) {
        loginBtn.setDisable(loading);
        loginBtn.setText(loading ? "Signing in…" : "Sign In");
    }
}