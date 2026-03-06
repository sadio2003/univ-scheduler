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
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Classe principale de l'application UNIV-SCHEDULER
 * Point d'entrée JavaFX
 *
 * @author UNIV-SCHEDULER Team
 * @version 1.0
 */
public class Main extends Application {

    private static Stage primaryStage;
    private static final String APP_TITLE = "UNIV-SCHEDULER - UIDT";
    private static final int MIN_WIDTH = 900;
    private static final int MIN_HEIGHT = 600;

    /**
     * Point d'entrée principal de l'application JavaFX
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // Afficher un splash screen au démarrage
        showSplashScreen();

        // Tester la connexion à la base de données en arrière-plan
        testDatabaseConnection();

        // Charger l'écran de connexion
        loadLoginScreen();
    }

    /**
     * Affiche un écran de démarrage (splash screen)
     */
    private void showSplashScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/univ/scheduler/fxml/SplashView.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 300);

            // Style sans barre de titre
            Stage splashStage = new Stage(StageStyle.UNDECORATED);
            splashStage.setScene(scene);
            splashStage.show();

            // Fermer automatiquement après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(splashStage::close);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (IOException e) {
            // Pas de splash screen si le fichier n'existe pas, continuer normalement
            System.out.println("Splash screen non trouvé, démarrage direct...");
        }
    }

    /**
     * Teste la connexion à la base de données
     */
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

    /**
     * Charge l'écran de connexion
     */
    private void loadLoginScreen() {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/univ/scheduler/fxml/LoginView.fxml")
            );

            Parent root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root, 900, 600);

            // Charger le fichier CSS
            try {
                String css = getClass().getResource("/com/univ/scheduler/css/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("Fichier CSS non trouvé, utilisation du style par défaut");
            }

            // Configurer la fenêtre
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);

            // Icône de l'application
            try {
                Image icon = new Image(getClass().getResourceAsStream("/com/univ/scheduler/images/logo_uidt.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Icône non trouvée, utilisation de l'icône par défaut");
            }

            // Centrer la fenêtre
            primaryStage.centerOnScreen();

            // Afficher
            primaryStage.show();

            // Gestionnaire de fermeture
            primaryStage.setOnCloseRequest(event -> {
                handleExit();
            });

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

    /**
     * Gère la fermeture de l'application
     */
    private void handleExit() {
        System.out.println("🔌 Fermeture de l'application...");

        // Fermer la connexion à la base de données
        try {
            DBConnection.closeConnection();
            System.out.println("✅ Connexion à la base de données fermée");
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }

        // Sauvegarder les préférences utilisateur si nécessaire
        saveUserPreferences();

        // Quitter
        Platform.exit();
        System.out.println("👋 Application terminée");
    }

    /**
     * Sauvegarde les préférences utilisateur
     */
    private void saveUserPreferences() {
        // Implémentation à venir
        // Sauvegarde de la taille de la fenêtre, position, etc.
    }

    /**
     * Affiche une erreur fatale et quitte
     */
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

    /**
     * Point d'entrée principal (méthode main)
     */
    public static void main(String[] args) {
        // Afficher la version Java au démarrage
        System.out.println("☕ Version Java: " + System.getProperty("java.version"));
        System.out.println("📦 OS: " + System.getProperty("os.name"));
        System.out.println("🚀 Démarrage de " + APP_TITLE + "...");

        // Lancer l'application JavaFX
        launch(args);
    }

    /**
     * Méthode utilitaire pour obtenir la scène principale
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Méthode utilitaire pour changer de vue
     * @param fxmlPath Chemin vers le fichier FXML
     * @param title Titre de la nouvelle fenêtre
     */
    public static void changeView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource(fxmlPath)
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // Appliquer le CSS
            try {
                String css = Main.class.getResource("/com/univ/scheduler/css/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                // Ignorer si le CSS n'existe pas
            }

            primaryStage.setTitle(APP_TITLE + " - " + title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du changement de vue: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Méthode utilitaire pour redémarrer l'application
     */
    public static void restart() {
        Platform.runLater(() -> {
            try {
                // Sauvegarder l'état
                DBConnection.closeConnection();

                // Relancer
                primaryStage.close();
                Platform.exit();

                // Redémarrer dans un nouveau processus
                ProcessBuilder pb = new ProcessBuilder(
                        "java",
                        "--module-path",
                        "C:\\javafx-sdk-21\\lib",
                        "--add-modules",
                        "javafx.controls,javafx.fxml",
                        "-jar",
                        "target/scheduler-1.0-SNAPSHOT.jar"
                );
                pb.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}