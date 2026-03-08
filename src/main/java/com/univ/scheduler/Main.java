package com.univ.scheduler;

import com.univ.scheduler.db.DBConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {

    private static Stage primaryStage;
    private static final String APP_TITLE = "UNIV-SCHEDULER - UIDT";
    private static final int MIN_WIDTH = 900;
    private static final int MIN_HEIGHT = 600;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showSplashScreen();
        testDatabaseConnection();
        loadLoginScreen();
    }

    private void showSplashScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/univ/scheduler/fxml/SplashView.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 300);

            Stage splashStage = new Stage(StageStyle.UNDECORATED);
            splashStage.setScene(scene);
            splashStage.show();

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(splashStage::close);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Splash screen non trouvé, démarrage direct...");
        }
    }

    private void testDatabaseConnection() {
        new Thread(() -> {
            try {
                Connection conn = DBConnection.getConnection();
                if (conn != null && !conn.isClosed()) {
                    System.out.println("✅ Connexion à la base de données établie avec succès");
                } else {
                    System.err.println("⚠️ Attention: Problème de connexion à la base de données");
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
            }
        }).start();
    }

    private void loadLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/univ/scheduler/fxml/LoginView.fxml")
            );

            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);

            try {
                String css = getClass().getResource("/com/univ/scheduler/css/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("Fichier CSS non trouvé, utilisation du style par défaut");
            }

            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);

            try {
                Image icon = new Image(getClass().getResourceAsStream("/com/univ/scheduler/images/logo_uidt.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Icône non trouvée, utilisation de l'icône par défaut");
            }

            primaryStage.centerOnScreen();
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> handleExit());

            System.out.println("🚀 Application démarrée avec succès");

        } catch (IOException e) {
            System.err.println("❌ Erreur fatale: Impossible de charger l'écran de connexion");
            e.printStackTrace();
            showErrorAndExit("Erreur de démarrage",
                    "Impossible de charger l'interface de connexion.\n" +
                            "Vérifiez que les fichiers FXML sont présents dans resources/fxml/");
        } catch (Exception e) {
            System.err.println("❌ Erreur fatale inattendue");
            e.printStackTrace();
            showErrorAndExit("Erreur inattendue",
                    "Une erreur inattendue s'est produite au démarrage:\n" + e.getMessage());
        }
    }

    private void handleExit() {
        System.out.println("🔌 Fermeture de l'application...");
        try {
            DBConnection.closeConnection();
            System.out.println("✅ Connexion à la base de données fermée");
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
        Platform.exit();
        System.out.println("👋 Application terminée");
    }

    private void showErrorAndExit(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur fatale");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
        });
    }

    public static void main(String[] args) {
        System.out.println("☕ Version Java: " + System.getProperty("java.version"));
        System.out.println("📦 OS: " + System.getProperty("os.name"));
        System.out.println("🚀 Démarrage de " + APP_TITLE + "...");
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void changeView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/univ/scheduler/fxml/" + fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);

            try {
                String css = Main.class.getResource("/com/univ/scheduler/css/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                // Ignorer
            }

            primaryStage.setTitle(APP_TITLE + " - " + title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du changement de vue: " + fxmlPath);
            e.printStackTrace();
        }
    }
}