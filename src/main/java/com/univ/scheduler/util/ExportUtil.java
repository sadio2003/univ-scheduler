package com.univ.scheduler.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import com.univ.scheduler.model.Seance;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilitaire pour l'export des données
 * Gère l'export en PDF, Excel, CSV
 */
public class ExportUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Exporte l'emploi du temps en CSV
     */
    public static boolean exportToCSV(List<Seance> seances, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'emploi du temps");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV", "*.csv")
        );
        fileChooser.setInitialFileName("emploi_du_temps.csv");

        File file = fileChooser.showSaveDialog(owner);
        if (file == null) return false;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // En-tête CSV
            writer.println("Jour;Heure;Matière;Enseignant;Classe;Salle");

            // Données
            for (Seance seance : seances) {
                writer.println(
                        seance.getJourSemaine() + ";" +
                                seance.getHeureDebut().format(TIME_FORMAT) + " - " +
                                seance.getHeureFin().format(TIME_FORMAT) + ";" +
                                seance.getCours().getNomMatiere() + ";" +
                                seance.getCours().getEnseignant().getNom() + ";" +
                                seance.getCours().getClasse() + " " +
                                (seance.getCours().getGroupe() != null ? seance.getCours().getGroupe() : "") + ";" +
                                seance.getSalle().getNumero()
                );
            }

            return true;
        } catch (IOException e) {
            AlertUtil.showError("Erreur d'export",
                    "Impossible d'exporter le fichier",
                    AlertUtil.exceptionToString(e));
            return false;
        }
    }

    /**
     * Exporte les salles en CSV
     */
    public static boolean exportSallesToCSV(List<?> salles, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des salles");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV", "*.csv")
        );
        fileChooser.setInitialFileName("salles.csv");

        File file = fileChooser.showSaveDialog(owner);
        if (file == null) return false;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Implémentation spécifique
            writer.println("Numéro;Capacité;Type;Bâtiment");
            return true;
        } catch (IOException e) {
            AlertUtil.showError("Erreur d'export",
                    "Impossible d'exporter le fichier",
                    AlertUtil.exceptionToString(e));
            return false;
        }
    }

    /**
     * Exporte les statistiques en PDF
     */
    public static boolean exportToPDF(String titre, String contenu, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("rapport.pdf");

        File file = fileChooser.showSaveDialog(owner);
        if (file == null) return false;

        try {
            // Implémentation PDF avec iText ou autre bibliothèque
            // À ajouter comme dépendance dans pom.xml
            AlertUtil.showInformation("Export PDF",
                    "Fonctionnalité à implémenter avec une bibliothèque PDF");
            return true;
        } catch (Exception e) {
            AlertUtil.showError("Erreur d'export",
                    "Impossible d'exporter le PDF",
                    AlertUtil.exceptionToString(e));
            return false;
        }
    }

    /**
     * Exporte en Excel (format XLSX)
     */
    public static boolean exportToExcel(String titre, List<String[]> data, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en Excel");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("Fichier Excel (ancien)", "*.xls")
        );
        fileChooser.setInitialFileName("rapport.xlsx");

        File file = fileChooser.showSaveDialog(owner);
        if (file == null) return false;

        try {
            // Implémentation Excel avec Apache POI
            // À ajouter comme dépendance dans pom.xml
            AlertUtil.showInformation("Export Excel",
                    "Fonctionnalité à implémenter avec Apache POI");
            return true;
        } catch (Exception e) {
            AlertUtil.showError("Erreur d'export",
                    "Impossible d'exporter le fichier Excel",
                    AlertUtil.exceptionToString(e));
            return false;
        }
    }
}