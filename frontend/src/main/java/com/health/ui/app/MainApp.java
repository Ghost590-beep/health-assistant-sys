package com.health.ui.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.health.ui.services.ReminderService;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/auth/Login.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 650);

        scene.getStylesheets().add(
                getClass().getResource("/css/global.css").toExternalForm());

        stage.setTitle("HealthAssist - Healthcare Management");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        ReminderService.start();
    }
    public static void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.err.println("FXML not found: " + fxmlPath);
                return;
            }
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(MainApp.class.getResource("/css/global.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}