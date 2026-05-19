package com.health.ui.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.health.ui.utils.SessionManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderService {

    private static ScheduledExecutorService scheduler;

    public static void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ReminderThread");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            if (!SessionManager.getInstance().isLoggedIn()) return;

            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://localhost:8080/api/appointments/upcoming")
                        .build();
                okhttp3.Response response = client.newCall(request).execute();
                String body = response.body().string();
                List<Map<String, Object>> list = new Gson().fromJson(body,
                        new TypeToken<List<Map<String, Object>>>(){}.getType());

                if (!list.isEmpty()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("⏰ Appointment Reminder");
                        alert.setHeaderText("You have upcoming appointments!");
                        alert.setContentText(list.size() + " appointment(s) coming up.");
                        alert.show();
                    });
                }
            } catch (Exception ignored) {}
        }, 10, 120, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (scheduler != null) scheduler.shutdown();
    }
}