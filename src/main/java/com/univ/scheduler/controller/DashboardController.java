package com.univ.scheduler.controller;

import com.univ.scheduler.model.Utilisateur;
import com.univ.scheduler.util.AlertUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private Label welcomeLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label userRoleLabel;
    @FXML private VBox menuVBox;
    @FXML private Button btnSalles;
    @FXML private Button btnBatiments;
    @FXML private Button btnCours;
    @FXML private Button btnEmploiTemps;
    @FXML private Button btnRecherche;
    @FXML private Button btnStatistiques;
    @FXML private Button btnDeconnexion;
    @FXML private Label notificationBadge;

    private Utilisateur currentUser;
    private Timer timer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMenuButtons();
        setupDateTimeUpdate();
        setupNotificationBadge();
        setupMenuHoverEffects();
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.currentUser = utilisateur;
        updateWelcomeMessage();
        configureMenuByRole();
    }

    private void updateWelcomeMessage() {
        String title = "";
        switch (currentUser.getRole()) {
            case "Admin":
                title = "Administrateur";
                break;
            case "Gestionnaire":
                title = "Gestionnaire";
                break;
            case "Enseignant":
                title = "Enseignant";
                break;
            case "Etudiant":
                title = "Étudiant";
                break;
        }
        welcomeLabel.setText("Bienvenue, " + currentUser.getNom());
        userRoleLabel.setText(title);
    }

    private void setupMenuButtons() {
        btnSalles.setOnAction(e -> loadView("GestionSallesView.fxml", "Gestion des salles"));
        btnBatiments.setOnAction(e -> loadView("GestionBatimentsView.fxml", "Gestion des bâtiments"));
        btnCours.setOnAction(e -> loadView("GestionCoursView.fxml", "Gestion des cours"));
        btnEmploiTemps.setOnAction(e -> loadView("EmploiDuTempsView.fxml", "Emploi du temps"));
        btnRecherche.setOnAction(e -> loadView("RechercheSallesView.fxml", "Recherche de salles"));
        btnStatistiques.setOnAction(e -> loadView("StatistiquesView.fxml", "Statistiques"));
        btnDeconnexion.setOnAction(e -> handleLogout());
    }

    private void setupMenuHoverEffects() {
        if (menuVBox == null) {
            // Alternative si menuVBox n'existe pas
            Button[] buttons = {btnSalles, btnBatiments, btnCours, btnEmploiTemps, btnRecherche, btnStatistiques};
            for (Button btn : buttons) {
                if (btn != null) {
                    btn.setOnMouseEntered(e ->
                            btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;")
                    );
                    btn.setOnMouseExited(e ->
                            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2c3e50;")
                    );
                }
            }
            return;
        }

        for (Node node : menuVBox.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setOnMouseEntered(e ->
                        btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;")
                );
                btn.setOnMouseExited(e ->
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2c3e50;")
                );
            }
        }
    }

    private void configureMenuByRole() {
        switch (currentUser.getRole()) {
            case "Admin":
                // Tous les boutons sont accessibles
                break;
            case "Gestionnaire":
                // Tout sauf statistiques peut-être
                break;
            case "Enseignant":
                btnBatiments.setVisible(false);
                btnStatistiques.setVisible(false);
                break;
            case "Etudiant":
                btnSalles.setVisible(false);
                btnBatiments.setVisible(false);
                btnCours.setVisible(false);
                btnStatistiques.setVisible(false);
                break;
        }
    }

    /**
     * Charge une vue dans la zone centrale
     */
    private void loadView(String fxmlFile, String title) {
        try {
            // ✅ CHEMIN CORRIGÉ
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/univ/scheduler/fxml/" + fxmlFile)
            );

            System.out.println("Chargement de: /com/univ/scheduler/fxml/" + fxmlFile);

            Parent view = loader.load();

            // Animation de transition
            FadeTransition ft = new FadeTransition(Duration.millis(300), view);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);

            mainPane.setCenter(view);
            ft.play();

            // Mettre à jour le titre
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setTitle("UNIV-SCHEDULER - " + title);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement: " + fxmlFile);
            e.printStackTrace();
            AlertUtil.showError("Erreur",
                    "Impossible de charger la vue",
                    "Fichier: " + fxmlFile + "\n" + e.getMessage());
        }
    }

    private void setupDateTimeUpdate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm:ss");

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    dateTimeLabel.setText(LocalDateTime.now().format(formatter));
                });
            }
        }, 0, 1000);
    }

    private void setupNotificationBadge() {
        int notificationCount = 3;
        if (notificationCount > 0) {
            notificationBadge.setText(String.valueOf(notificationCount));
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }
    }

    private void handleLogout() {
        boolean confirm = AlertUtil.showConfirmation("Déconnexion",
                "Êtes-vous sûr de vouloir vous déconnecter ?");

        if (confirm) {
            try {
                if (timer != null) {
                    timer.cancel();
                }

                LoginController.SessionManager.getInstance().logout();

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/univ/scheduler/fxml/LoginView.fxml")
                );
                Parent root = loader.load();

                Stage stage = (Stage) mainPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("UNIV-SCHEDULER - Connexion");

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur",
                        "Erreur lors de la déconnexion",
                        e.getMessage());
            }
        }
    }

    @FXML
    private void handleNotifications(MouseEvent event) {
        AlertUtil.showInformation("Notifications",
                "Vous avez 3 notifications non lues.\n\n" +
                        "1. Réunion à 14h00\n" +
                        "2. Changement de salle pour le cours LI2\n" +
                        "3. Demande de réservation en attente");
    }

    @FXML
    private void handleProfile(MouseEvent event) {
        AlertUtil.showInformation("Profil utilisateur",
                "Nom: " + currentUser.getNom() + "\n" +
                        "Email: " + currentUser.getEmail() + "\n" +
                        "Rôle: " + currentUser.getRole() + "\n" +
                        "Dernière connexion: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    public void cleanup() {
        if (timer != null) {
            timer.cancel();
        }
    }
}