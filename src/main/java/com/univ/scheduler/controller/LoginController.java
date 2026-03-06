package com.univ.scheduler.controller;

import com.univ.scheduler.db.UtilisateurDAO;
import com.univ.scheduler.model.Utilisateur;
import com.univ.scheduler.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la fenêtre de connexion
 * Gère l'authentification des utilisateurs
 */
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private CheckBox rememberMeCheckBox;

    private UtilisateurDAO utilisateurDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utilisateurDAO = new UtilisateurDAO();
        setupEnterKeyHandler();
        loadingIndicator.setVisible(false);
        loadSavedCredentials();
    }

    /**
     * Configuration de la touche Entrée pour soumettre le formulaire
     */
    private void setupEnterKeyHandler() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    /**
     * Charge les identifiants sauvegardés (si rememberMe est coché)
     */
    private void loadSavedCredentials() {
        // À implémenter avec Preferences API
        // emailField.setText(Preferences.userRoot().get("email", ""));
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation des champs
        if (!validateInputs(email, password)) {
            return;
        }

        // Afficher le chargement
        setLoadingState(true);

        // Simuler un délai réseau (à retirer en production)
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulation
                javafx.application.Platform.runLater(() -> {
                    authenticateUser(email, password);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Valide les entrées utilisateur
     */
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            showStatus("Veuillez saisir votre email", Alert.AlertType.WARNING);
            emailField.requestFocus();
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showStatus("Format d'email invalide", Alert.AlertType.WARNING);
            emailField.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showStatus("Veuillez saisir votre mot de passe", Alert.AlertType.WARNING);
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Authentifie l'utilisateur
     */
    private void authenticateUser(String email, String password) {
        Utilisateur utilisateur = utilisateurDAO.authenticate(email, password);

        if (utilisateur != null) {
            // Sauvegarder la session
            SessionManager.getInstance().setCurrentUser(utilisateur);

            // Sauvegarder l'email si "Se souvenir de moi" est coché
            if (rememberMeCheckBox.isSelected()) {
                saveCredentials(email);
            }

            showStatus("Connexion réussie !", Alert.AlertType.INFORMATION);
            openDashboard(utilisateur);
        } else {
            setLoadingState(false);
            showStatus("Email ou mot de passe incorrect", Alert.AlertType.ERROR);
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    /**
     * Sauvegarde les identifiants (à implémenter avec Preferences)
     */
    private void saveCredentials(String email) {
        // Preferences.userRoot().put("email", email);
    }

    /**
     * Ouvre le tableau de bord
     */
    private void openDashboard(Utilisateur utilisateur) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/univ/scheduler/fxml/DashboardView.fxml")
            );
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUtilisateur(utilisateur);

            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Animation de transition
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.setTitle("UNIV-SCHEDULER - Tableau de bord");

        } catch (IOException e) {
            e.printStackTrace();
            setLoadingState(false);
            AlertUtil.showError("Erreur",
                    "Impossible de charger le tableau de bord",
                    e.getMessage());
        }
    }

    /**
     * Gère l'état de chargement
     */
    private void setLoadingState(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        rememberMeCheckBox.setDisable(loading);
    }

    /**
     * Affiche un message dans le label de statut
     */
    private void showStatus(String message, Alert.AlertType type) {
        statusLabel.setText(message);

        switch (type) {
            case ERROR:
                statusLabel.setStyle("-fx-text-fill: #d32f2f;");
                break;
            case WARNING:
                statusLabel.setStyle("-fx-text-fill: #f57c00;");
                break;
            case INFORMATION:
                statusLabel.setStyle("-fx-text-fill: #388e3c;");
                break;
            default:
                statusLabel.setStyle("-fx-text-fill: #666666;");
        }

        statusLabel.setVisible(true);
    }

    @FXML
    private void handleForgotPassword() {
        AlertUtil.showInformation("Mot de passe oublié",
                "Veuillez contacter l'administrateur à : admin@univ.sn");
    }

    @FXML
    private void handleQuit() {
        boolean confirm = AlertUtil.showConfirmation("Quitter",
                "Êtes-vous sûr de vouloir quitter l'application ?");
        if (confirm) {
            javafx.application.Platform.exit();
        }
    }

    /**
     * Gestionnaire de session (Singleton)
     */
    public static class SessionManager {
        private static SessionManager instance;
        private Utilisateur currentUser;

        private SessionManager() {}

        public static SessionManager getInstance() {
            if (instance == null) {
                instance = new SessionManager();
            }
            return instance;
        }

        public void setCurrentUser(Utilisateur user) {
            this.currentUser = user;
        }

        public Utilisateur getCurrentUser() {
            return currentUser;
        }

        public boolean isAuthenticated() {
            return currentUser != null;
        }

        public void logout() {
            currentUser = null;
        }
    }
}