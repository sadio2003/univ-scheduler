package com.univ.scheduler.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Utilitaire pour les boîtes de dialogue
 * Centralise la création des alertes pour une interface cohérente
 */
public class AlertUtil {

    /**
     * Affiche une alerte d'information
     */
    public static void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte de succès
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #d4edda;");
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'avertissement
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'erreur
     */
    public static void showError(String title, String message, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);

        if (details != null && !details.isEmpty()) {
            TextArea textArea = new TextArea(details);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);

            alert.getDialogPane().setExpandableContent(expContent);
        }

        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte de confirmation
     * @return true si l'utilisateur a confirmé
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Style commun pour toutes les alertes
     */
    private static void styleAlert(Alert alert) {
        alert.getDialogPane().setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;"
        );
    }

    /**
     * Convertit une exception en chaîne de caractères
     */
    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}