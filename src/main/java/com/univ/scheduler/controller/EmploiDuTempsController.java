package com.univ.scheduler.controller;

import com.univ.scheduler.db.CoursDAO;
import com.univ.scheduler.db.SalleDAO;
import com.univ.scheduler.db.SeanceDAO;
import com.univ.scheduler.model.Cours;           // ✅ AJOUTÉ
import com.univ.scheduler.model.Salle;           // ✅ AJOUTÉ
import com.univ.scheduler.model.Seance;          // ✅ AJOUTÉ
import com.univ.scheduler.util.AlertUtil;
import com.univ.scheduler.util.ConflitUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des emplois du temps
 * Gère la planification des séances et la détection des conflits
 */
public class EmploiDuTempsController implements Initializable {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> semaineCombo;
    @FXML private ComboBox<String> vueCombo;
    @FXML private Button btnPrecedent;
    @FXML private Button btnSuivant;
    @FXML private Button btnAujourdhui;
    @FXML private Button btnAjouterSeance;
    @FXML private Button btnExporter;
    @FXML private Button btnImprimer;

    @FXML private GridPane emploiDuTempsGrid;
    @FXML private ListView<String> conflitsListView;
    @FXML private Label periodeLabel;
    @FXML private Label totalSeancesLabel;
    @FXML private Label tauxOccupationLabel;

    @FXML private TabPane tabPane;
    @FXML private Tab tabVueGlobale;
    @FXML private Tab tabParClasse;
    @FXML private Tab tabParSalle;
    @FXML private Tab tabParEnseignant;

    @FXML private ComboBox<String> classeCombo;
    @FXML private ComboBox<Salle> salleCombo;
    @FXML private ComboBox<String> enseignantCombo;

    private SeanceDAO seanceDAO;
    private CoursDAO coursDAO;
    private SalleDAO salleDAO;

    private ObservableList<Seance> seancesObservable;
    private LocalDate currentDate;

    // Jours de la semaine
    private final String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private final String[] heures = {"08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        seanceDAO = new SeanceDAO();
        coursDAO = new CoursDAO();
        salleDAO = new SalleDAO();

        currentDate = LocalDate.now();

        setupControls();
        setupVues();
        loadData();
        buildEmploiDuTemps();
        verifierConflits();
    }

    /**
     * Configuration des contrôles
     */
    private void setupControls() {
        // Date picker
        datePicker.setValue(currentDate);
        datePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ?
                        LocalDate.parse(string, formatter) : null;
            }
        });

        // Semaine combo
        semaineCombo.setItems(FXCollections.observableArrayList(
                "Semaine 1", "Semaine 2", "Semaine 3", "Semaine 4", "Semaine 5", "Semaine 6"
        ));
        semaineCombo.setValue("Semaine en cours");

        // Vue combo
        vueCombo.setItems(FXCollections.observableArrayList(
                "Jour", "Semaine", "Mois"
        ));
        vueCombo.setValue("Semaine");

        // Boutons navigation
        btnPrecedent.setOnAction(e -> naviguerPrecedent());
        btnSuivant.setOnAction(e -> naviguerSuivant());
        btnAujourdhui.setOnAction(e -> allerAujourdhui());
        btnAjouterSeance.setOnAction(e -> ajouterSeance());
        btnExporter.setOnAction(e -> exporterEmploiDuTemps());
        btnImprimer.setOnAction(e -> imprimerEmploiDuTemps());

        datePicker.setOnAction(e -> buildEmploiDuTemps());
        vueCombo.setOnAction(e -> buildEmploiDuTemps());
    }

    /**
     * Configuration des différentes vues
     */
    private void setupVues() {
        // Vue par classe
        ObservableList<String> classes = FXCollections.observableArrayList(
                "LI1", "LI2", "LI3", "GI1", "GI2", "GI3", "MI", "M1", "M2"
        );
        classeCombo.setItems(classes);
        classeCombo.setValue("LI2");
        classeCombo.setOnAction(e -> buildEmploiDuTemps());

        // Vue par salle
        List<Salle> salles = salleDAO.getAll();
        salleCombo.setItems(FXCollections.observableArrayList(salles));
        salleCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Salle salle) {
                return salle != null ? salle.getNumero() : "";
            }

            @Override
            public Salle fromString(String string) {
                return salles.stream()
                        .filter(s -> s.getNumero().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        if (!salles.isEmpty()) {
            salleCombo.setValue(salles.get(0));
        }
        salleCombo.setOnAction(e -> buildEmploiDuTemps());

        // Vue par enseignant
        ObservableList<String> enseignants = FXCollections.observableArrayList(
                "Dr. Diop", "Pr. Ndiaye", "M. Fall", "Mme. Seck", "Dr. Cissé"
        );
        enseignantCombo.setItems(enseignants);
        enseignantCombo.setValue("Dr. Diop");
        enseignantCombo.setOnAction(e -> buildEmploiDuTemps());

        // Changer de tab
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> buildEmploiDuTemps()
        );
    }

    /**
     * Charge les données
     */
    private void loadData() {
        seancesObservable = FXCollections.observableArrayList(seanceDAO.getAll());
    }

    /**
     * Construit l'emploi du temps
     */
    private void buildEmploiDuTemps() {
        emploiDuTempsGrid.getChildren().clear();
        emploiDuTempsGrid.getColumnConstraints().clear();

        // Titre de la période
        updatePeriodeLabel();

        // Construction selon la vue sélectionnée
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab == tabVueGlobale) {
            buildVueGlobale();
        } else if (selectedTab == tabParClasse) {
            buildVueParClasse();
        } else if (selectedTab == tabParSalle) {
            buildVueParSalle();
        } else if (selectedTab == tabParEnseignant) {
            buildVueParEnseignant();
        }

        updateStatistiques();
    }

    /**
     * Construit la vue globale
     */
    private void buildVueGlobale() {
        javafx.scene.layout.ColumnConstraints[] constraints = createColumnConstraints(12);
        emploiDuTempsGrid.getColumnConstraints().addAll(constraints);

        // En-têtes des jours
        for (int j = 0; j < jours.length; j++) {
            Label header = new Label(jours[j]);
            header.getStyleClass().add("emploi-temps-header");
            header.setPrefHeight(40);
            GridPane.setConstraints(header, j + 1, 0);
            emploiDuTempsGrid.getChildren().add(header);
        }

        // Heures et cellules
        for (int h = 0; h < heures.length; h++) {
            // Label heure
            Label heureLabel = new Label(heures[h]);
            heureLabel.getStyleClass().add("emploi-temps-heure");
            heureLabel.setPrefHeight(60);
            GridPane.setConstraints(heureLabel, 0, h + 1);
            emploiDuTempsGrid.getChildren().add(heureLabel);

            // Cellules pour chaque jour
            for (int j = 0; j < jours.length; j++) {
                VBox cell = createSeanceCell(j, h);
                GridPane.setConstraints(cell, j + 1, h + 1);
                emploiDuTempsGrid.getChildren().add(cell);
            }
        }
    }

    /**
     * Construit la vue par classe
     */
    private void buildVueParClasse() {
        String classe = classeCombo.getValue();

        javafx.scene.layout.ColumnConstraints[] constraints = createColumnConstraints(4);
        emploiDuTempsGrid.getColumnConstraints().addAll(constraints);

        // En-têtes
        Label jourHeader = new Label("Jour");
        jourHeader.getStyleClass().add("emploi-temps-header");
        GridPane.setConstraints(jourHeader, 0, 0);

        Label horaireHeader = new Label("Horaire");
        horaireHeader.getStyleClass().add("emploi-temps-header");
        GridPane.setConstraints(horaireHeader, 1, 0);

        Label coursHeader = new Label("Cours");
        coursHeader.getStyleClass().add("emploi-temps-header");
        GridPane.setConstraints(coursHeader, 2, 0);

        Label salleHeader = new Label("Salle");
        salleHeader.getStyleClass().add("emploi-temps-header");
        GridPane.setConstraints(salleHeader, 3, 0);

        emploiDuTempsGrid.getChildren().addAll(jourHeader, horaireHeader, coursHeader, salleHeader);

        // Filtrer les séances pour cette classe
        List<Seance> seancesClasse = seancesObservable.stream()
                .filter(s -> classe.equals(s.getCours().getClasse()))
                .collect(Collectors.toList());

        int row = 1;
        for (Seance seance : seancesClasse) {
            Label jour = new Label(seance.getJourSemaine());
            Label horaire = new Label(seance.getHeureDebut() + " - " + seance.getHeureFin());
            Label cours = new Label(seance.getCours().getNomMatiere());
            Label salle = new Label(seance.getSalle().getNumero());

            jour.setPrefHeight(40);
            horaire.setPrefHeight(40);
            cours.setPrefHeight(40);
            salle.setPrefHeight(40);

            GridPane.setConstraints(jour, 0, row);
            GridPane.setConstraints(horaire, 1, row);
            GridPane.setConstraints(cours, 2, row);
            GridPane.setConstraints(salle, 3, row);

            emploiDuTempsGrid.getChildren().addAll(jour, horaire, cours, salle);
            row++;
        }
    }

    /**
     * Construit la vue par salle
     */
    private void buildVueParSalle() {
        Salle salle = salleCombo.getValue();
        if (salle == null) return;

        emploiDuTempsGrid.getChildren().clear();

        Label infoLabel = new Label("Emploi du temps de la salle " + salle.getNumero());
        infoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        GridPane.setConstraints(infoLabel, 0, 0, 4, 1);
        emploiDuTempsGrid.getChildren().add(infoLabel);

        // TODO: Ajouter l'affichage des séances pour cette salle
    }

    /**
     * Construit la vue par enseignant
     */
    private void buildVueParEnseignant() {
        String enseignant = enseignantCombo.getValue();

        emploiDuTempsGrid.getChildren().clear();

        Label infoLabel = new Label("Emploi du temps de " + enseignant);
        infoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        GridPane.setConstraints(infoLabel, 0, 0, 4, 1);
        emploiDuTempsGrid.getChildren().add(infoLabel);

        // TODO: Ajouter l'affichage des séances pour cet enseignant
    }

    /**
     * Crée une cellule pour une séance
     */
    private VBox createSeanceCell(int jourIndex, int heureIndex) {
        VBox cell = new VBox(5);
        cell.getStyleClass().add("emploi-temps-cell");
        cell.setPrefHeight(60);
        cell.setPadding(new Insets(5));

        String jour = jours[jourIndex];
        String heure = heures[heureIndex];

        // Chercher les séances à ce créneau
        List<Seance> seancesCreneau = seancesObservable.stream()
                .filter(s -> s.getJourSemaine().equalsIgnoreCase(jour) &&
                        s.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")).equals(heure))
                .collect(Collectors.toList());

        for (Seance seance : seancesCreneau) {
            Label coursLabel = new Label(seance.getCours().getNomMatiere());
            coursLabel.getStyleClass().add("seance-label");
            coursLabel.setTooltip(new Tooltip(
                    seance.getCours().getNomMatiere() + "\n" +
                            "Salle: " + seance.getSalle().getNumero() + "\n" +
                            "Enseignant: " + seance.getCours().getEnseignant().getNom()
            ));

            coursLabel.setOnMouseClicked(e -> showSeanceDetails(seance));

            cell.getChildren().add(coursLabel);
        }

        return cell;
    }

    /**
     * Affiche les détails d'une séance
     */
    private void showSeanceDetails(Seance seance) {
        String message = String.format(
                "📚 DÉTAILS DE LA SÉANCE\n\n" +
                        "📖 Cours: %s\n" +
                        "👨‍🏫 Enseignant: %s\n" +
                        "🎓 Classe: %s %s\n" +
                        "🏢 Salle: %s\n" +
                        "📅 Jour: %s\n" +
                        "⏰ Horaire: %s - %s\n" +
                        "⏱️ Durée: %d minutes\n\n" +
                        "📝 Description: %s",
                seance.getCours().getNomMatiere(),
                seance.getCours().getEnseignant().getNom(),
                seance.getCours().getClasse(),
                seance.getCours().getGroupe() != null ? seance.getCours().getGroupe() : "",
                seance.getSalle().getNumero(),
                seance.getJourSemaine(),
                seance.getHeureDebut(),
                seance.getHeureFin(),
                seance.getDuree(),
                "Séance de cours"
        );

        AlertUtil.showInformation("Détails de la séance", message);
    }

    /**
     * Vérifie les conflits
     */
    private void verifierConflits() {
        conflitsListView.getItems().clear();

        List<String> conflits = ConflitUtil.verifierTousConflits(seancesObservable);

        if (conflits.isEmpty()) {
            conflitsListView.getItems().add("✅ Aucun conflit détecté");
            conflitsListView.setStyle("-fx-text-fill: green;");
        } else {
            conflitsListView.setItems(FXCollections.observableArrayList(conflits));
        }
    }

    /**
     * Ajoute une nouvelle séance
     */
    private void ajouterSeance() {
        // Créer un dialogue personnalisé
        Dialog<Seance> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une séance");
        dialog.setHeaderText("Planifier une nouvelle séance");

        // Boutons
        ButtonType ajouterButton = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ajouterButton, ButtonType.CANCEL);

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Cours> coursCombo = new ComboBox<>();
        coursCombo.setItems(FXCollections.observableArrayList(coursDAO.getAll()));

        ComboBox<Salle> salleCombo = new ComboBox<>();
        salleCombo.setItems(FXCollections.observableArrayList(salleDAO.getAll()));

        ComboBox<String> jourCombo = new ComboBox<>();
        jourCombo.setItems(FXCollections.observableArrayList(jours));

        TextField heureDebutField = new TextField();
        heureDebutField.setPromptText("08:00");

        TextField dureeField = new TextField();
        dureeField.setPromptText("90");

        grid.add(new Label("Cours:"), 0, 0);
        grid.add(coursCombo, 1, 0);
        grid.add(new Label("Salle:"), 0, 1);
        grid.add(salleCombo, 1, 1);
        grid.add(new Label("Jour:"), 0, 2);
        grid.add(jourCombo, 1, 2);
        grid.add(new Label("Heure début:"), 0, 3);
        grid.add(heureDebutField, 1, 3);
        grid.add(new Label("Durée (min):"), 0, 4);
        grid.add(dureeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Validation
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ajouterButton) {
                // Créer et retourner la séance
                Seance seance = new Seance();
                seance.setCours(coursCombo.getValue());
                seance.setSalle(salleCombo.getValue());
                seance.setJourSemaine(jourCombo.getValue());
                seance.setHeureDebut(LocalTime.parse(heureDebutField.getText()));
                seance.setDuree(Integer.parseInt(dureeField.getText()));
                return seance;
            }
            return null;
        });

        Optional<Seance> result = dialog.showAndWait();

        result.ifPresent(seance -> {
            // Vérifier les conflits avant d'ajouter
            if (ConflitUtil.verifierConflit(seance, seancesObservable)) {
                AlertUtil.showWarning("Conflit détecté",
                        "Cette séance est en conflit avec une séance existante !");
            } else {
                if (seanceDAO.insert(seance)) {
                    AlertUtil.showSuccess("Succès", "Séance ajoutée avec succès");
                    loadData();
                    buildEmploiDuTemps();
                    verifierConflits();
                }
            }
        });
    }

    /**
     * Navigation précédente
     */
    private void naviguerPrecedent() {
        if ("Semaine".equals(vueCombo.getValue())) {
            currentDate = currentDate.minusWeeks(1);
        } else if ("Jour".equals(vueCombo.getValue())) {
            currentDate = currentDate.minusDays(1);
        } else {
            currentDate = currentDate.minusMonths(1);
        }
        datePicker.setValue(currentDate);
        buildEmploiDuTemps();
    }

    /**
     * Navigation suivante
     */
    private void naviguerSuivant() {
        if ("Semaine".equals(vueCombo.getValue())) {
            currentDate = currentDate.plusWeeks(1);
        } else if ("Jour".equals(vueCombo.getValue())) {
            currentDate = currentDate.plusDays(1);
        } else {
            currentDate = currentDate.plusMonths(1);
        }
        datePicker.setValue(currentDate);
        buildEmploiDuTemps();
    }

    /**
     * Aller à aujourd'hui
     */
    private void allerAujourdhui() {
        currentDate = LocalDate.now();
        datePicker.setValue(currentDate);
        buildEmploiDuTemps();
    }

    /**
     * Met à jour le label de période
     */
    private void updatePeriodeLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        periodeLabel.setText("Période du " +
                currentDate.format(formatter) + " au " +
                currentDate.plusDays(6).format(formatter));
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistiques() {
        totalSeancesLabel.setText(String.valueOf(seancesObservable.size()));

        // Calcul du taux d'occupation
        int totalCreneaux = jours.length * heures.length;
        int creneauxOccupes = seancesObservable.size();
        double taux = (double) creneauxOccupes / totalCreneaux * 100;
        tauxOccupationLabel.setText(String.format("%.1f%%", taux));
    }

    /**
     * Exporte l'emploi du temps
     */
    private void exporterEmploiDuTemps() {
        AlertUtil.showInformation("Export",
                "Export de l'emploi du temps en cours...\n" +
                        "Format: PDF\n" +
                        "Période: " + periodeLabel.getText());
    }

    /**
     * Imprime l'emploi du temps
     */
    private void imprimerEmploiDuTemps() {
        AlertUtil.showInformation("Impression",
                "Préparation de l'impression...");
    }

    /**
     * Crée les contraintes de colonnes pour la grille
     */
    private javafx.scene.layout.ColumnConstraints[] createColumnConstraints(int count) {
        javafx.scene.layout.ColumnConstraints[] constraints =
                new javafx.scene.layout.ColumnConstraints[count];

        for (int i = 0; i < count; i++) {
            constraints[i] = new javafx.scene.layout.ColumnConstraints();
            constraints[i].setPercentWidth(100.0 / count);
            constraints[i].setFillWidth(true);
        }

        return constraints;
    }
}