package com.univ.scheduler.controller;

import com.univ.scheduler.db.SalleDAO;
import com.univ.scheduler.db.SeanceDAO;
import com.univ.scheduler.model.Salle;
import com.univ.scheduler.model.Seance;
import com.univ.scheduler.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Contrôleur pour les statistiques et rapports
 * Affiche des graphiques et indicateurs de performance
 */
public class StatistiquesController implements Initializable {

    @FXML private TabPane statistiquesTabPane;

    // Onglet Vue d'ensemble
    @FXML private Label totalSallesLabel;
    @FXML private Label totalCoursLabel;
    @FXML private Label totalSeancesLabel;
    @FXML private Label totalEnseignantsLabel;
    @FXML private Label periodeLabel;

    @FXML private ProgressBar occupationGlobaleBar;
    @FXML private Label occupationGlobaleLabel;

    @FXML private ProgressBar utilisationSallesBar;
    @FXML private Label utilisationSallesLabel;

    // Graphiques
    @FXML private BarChart<String, Number> occupationChart;
    @FXML private CategoryAxis occupationXAxis;
    @FXML private NumberAxis occupationYAxis;

    @FXML private PieChart typeSallesChart;
    @FXML private PieChart equipementsChart;

    @FXML private LineChart<String, Number> tendanceChart;
    @FXML private CategoryAxis tendanceXAxis;
    @FXML private NumberAxis tendanceYAxis;

    // Onglet Détails
    @FXML private TableView<Salle> topSallesTable;
    @FXML private TableView<Salle> sallesCritiquesTable;

    @FXML private GridPane heatMapGrid;

    private SalleDAO salleDAO;
    private SeanceDAO seanceDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        salleDAO = new SalleDAO();
        seanceDAO = new SeanceDAO();

        setupPeriod();
        loadStatistiquesGlobales();
        setupOccupationChart();
        setupTypeSallesChart();
        setupEquipementsChart();
        setupTendanceChart();
        setupHeatMap();
    }

    /**
     * Configure la période d'affichage
     */
    private void setupPeriod() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        periodeLabel.setText("Période du " +
                now.withDayOfMonth(1).format(formatter) + " au " +
                now.withDayOfMonth(now.lengthOfMonth()).format(formatter));
    }

    /**
     * Charge les statistiques globales
     */
    private void loadStatistiquesGlobales() {
        List<Salle> salles = salleDAO.getAll();
        List<Seance> seances = seanceDAO.getAll();

        totalSallesLabel.setText(String.valueOf(salles.size()));

        // Compter les cours uniques
        long coursCount = seances.stream()
                .map(s -> s.getCours().getId())
                .distinct()
                .count();
        totalCoursLabel.setText(String.valueOf(coursCount));

        totalSeancesLabel.setText(String.valueOf(seances.size()));

        // Compter les enseignants
        long enseignantsCount = seances.stream()
                .map(s -> s.getCours().getIdEnseignant())
                .distinct()
                .count();
        totalEnseignantsLabel.setText(String.valueOf(enseignantsCount));

        // Taux d'occupation global
        double occupation = calculerTauxOccupation(seances, salles);
        occupationGlobaleBar.setProgress(occupation / 100);
        occupationGlobaleLabel.setText(String.format("%.1f%%", occupation));

        // Utilisation des salles
        double utilisation = calculerUtilisationSalles(seances, salles);
        utilisationSallesBar.setProgress(utilisation / 100);
        utilisationSallesLabel.setText(String.format("%.1f%%", utilisation));
    }

    /**
     * Calcule le taux d'occupation
     */
    private double calculerTauxOccupation(List<Seance> seances, List<Salle> salles) {
        // Simulation - à remplacer par un vrai calcul
        return 65.5;
    }

    /**
     * Calcule le taux d'utilisation des salles
     */
    private double calculerUtilisationSalles(List<Seance> seances, List<Salle> salles) {
        // Simulation - à remplacer par un vrai calcul
        return 42.3;
    }

    /**
     * Configure le graphique d'occupation
     */
    private void setupOccupationChart() {
        occupationChart.setTitle("Taux d'occupation par jour");
        occupationXAxis.setLabel("Jour");
        occupationYAxis.setLabel("Taux d'occupation (%)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Occupation");

        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
        double[] valeurs = {75, 82, 68, 91, 55, 30};

        for (int i = 0; i < jours.length; i++) {
            series.getData().add(new XYChart.Data<>(jours[i], valeurs[i]));
        }

        occupationChart.getData().add(series);
    }

    /**
     * Configure le graphique des types de salles
     */
    private void setupTypeSallesChart() {
        typeSallesChart.setTitle("Répartition par type de salle");

        List<Salle> salles = salleDAO.getAll();

        Map<String, Long> countByType = salles.stream()
                .collect(Collectors.groupingBy(Salle::getType, Collectors.counting()));

        for (Map.Entry<String, Long> entry : countByType.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            typeSallesChart.getData().add(slice);
        }

        // Couleurs personnalisées
        typeSallesChart.getData().forEach(data -> {
            if (data.getName().startsWith("TD")) {
                data.getNode().setStyle("-fx-pie-color: #3498db;");
            } else if (data.getName().startsWith("TP")) {
                data.getNode().setStyle("-fx-pie-color: #2ecc71;");
            } else if (data.getName().startsWith("Amphi")) {
                data.getNode().setStyle("-fx-pie-color: #e74c3c;");
            }
        });
    }

    /**
     * Configure le graphique des équipements
     */
    private void setupEquipementsChart() {
        equipementsChart.setTitle("Équipements les plus courants");

        // Données simulées
        equipementsChart.getData().addAll(
                new PieChart.Data("Vidéoprojecteur (45)", 45),
                new PieChart.Data("Tableau interactif (32)", 32),
                new PieChart.Data("Climatisation (28)", 28),
                new PieChart.Data("Ordinateurs (25)", 25),
                new PieChart.Data("Tableau blanc (15)", 15)
        );
    }

    /**
     * Configure le graphique de tendance
     */
    private void setupTendanceChart() {
        tendanceChart.setTitle("Évolution du taux d'occupation");
        tendanceXAxis.setLabel("Semaine");
        tendanceYAxis.setLabel("Taux d'occupation (%)");

        XYChart.Series<String, Number> series2024 = new XYChart.Series<>();
        series2024.setName("2024");

        String[] semaines = {"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"};
        double[] valeurs = {55, 62, 68, 71, 65, 73, 78, 82};

        for (int i = 0; i < semaines.length; i++) {
            series2024.getData().add(new XYChart.Data<>(semaines[i], valeurs[i]));
        }

        XYChart.Series<String, Number> series2023 = new XYChart.Series<>();
        series2023.setName("2023");

        double[] valeurs2023 = {48, 52, 58, 60, 55, 62, 65, 68};

        for (int i = 0; i < semaines.length; i++) {
            series2023.getData().add(new XYChart.Data<>(semaines[i], valeurs2023[i]));
        }

        tendanceChart.getData().addAll(series2024, series2023);
    }

    /**
     * Configure la heat map d'occupation
     */
    private void setupHeatMap() {
        heatMapGrid.getChildren().clear();

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        String[] heures = {"8h", "9h", "10h", "11h", "12h", "14h", "15h", "16h", "17h", "18h"};

        // En-têtes des jours
        for (int j = 0; j < jours.length; j++) {
            Label jourLabel = new Label(jours[j]);
            jourLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            jourLabel.setPrefSize(100, 30);
            GridPane.setConstraints(jourLabel, j + 1, 0);
            heatMapGrid.getChildren().add(jourLabel);
        }

        // En-têtes des heures
        for (int h = 0; h < heures.length; h++) {
            Label heureLabel = new Label(heures[h]);
            heureLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            heureLabel.setPrefSize(80, 30);
            GridPane.setConstraints(heureLabel, 0, h + 1);
            heatMapGrid.getChildren().add(heureLabel);
        }

        // Cellules de la heat map
        for (int j = 0; j < jours.length; j++) {
            for (int h = 0; h < heures.length; h++) {
                VBox cell = createHeatMapCell(calculerTauxOccupation(j, h));
                GridPane.setConstraints(cell, j + 1, h + 1);
                heatMapGrid.getChildren().add(cell);
            }
        }
    }

    /**
     * Crée une cellule pour la heat map
     */
    private VBox createHeatMapCell(double taux) {
        VBox cell = new VBox();
        cell.setPrefSize(100, 40);
        cell.setAlignment(javafx.geometry.Pos.CENTER);

        Label tauxLabel = new Label(String.format("%.0f%%", taux));
        tauxLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        // Couleur selon le taux
        String color;
        if (taux < 30) {
            color = "#2ecc71"; // Vert
        } else if (taux < 60) {
            color = "#f1c40f"; // Jaune
        } else if (taux < 80) {
            color = "#e67e22"; // Orange
        } else {
            color = "#e74c3c"; // Rouge
        }

        cell.setStyle("-fx-background-color: " + color + "20; -fx-border-color: #ddd; -fx-border-width: 0.5;");
        cell.getChildren().add(tauxLabel);

        Tooltip tooltip = new Tooltip("Taux d'occupation: " + String.format("%.1f%%", taux));
        Tooltip.install(cell, tooltip);

        return cell;
    }

    /**
     * Calcule le taux d'occupation pour un jour et une heure
     */
    private double calculerTauxOccupation(int jour, int heure) {
        // Simulation - à remplacer par un vrai calcul
        double[][] data = {
                {45, 60, 75, 80, 50, 30, 65, 70, 55, 40}, // Lundi
                {50, 65, 80, 85, 55, 35, 70, 75, 60, 45}, // Mardi
                {40, 55, 70, 75, 45, 25, 60, 65, 50, 35}, // Mercredi
                {55, 70, 85, 90, 60, 40, 75, 80, 65, 50}, // Jeudi
                {35, 50, 65, 70, 40, 20, 55, 60, 45, 30}, // Vendredi
                {20, 25, 30, 35, 15, 10, 25, 30, 20, 15}  // Samedi
        };

        return data[jour][heure];
    }

    @FXML
    private void exporterRapportPDF() {
        AlertUtil.showInformation("Export PDF",
                "Génération du rapport PDF en cours...");
    }

    @FXML
    private void exporterRapportExcel() {
        AlertUtil.showInformation("Export Excel",
                "Génération du rapport Excel en cours...");
    }

    @FXML
    private void imprimerRapport() {
        AlertUtil.showInformation("Impression",
                "Préparation de l'impression...");
    }

    @FXML
    private void actualiserDonnees() {
        loadStatistiquesGlobales();
        AlertUtil.showSuccess("Actualisation",
                "Les données ont été actualisées avec succès");
    }
}