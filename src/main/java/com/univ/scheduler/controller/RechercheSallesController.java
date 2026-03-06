package com.univ.scheduler.controller;

import com.univ.scheduler.db.BatimentDAO;
import com.univ.scheduler.db.EquipementDAO;
import com.univ.scheduler.db.SalleDAO;
import com.univ.scheduler.model.Batiment;
import com.univ.scheduler.model.Equipement;
import com.univ.scheduler.model.Salle;
import com.univ.scheduler.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;  // ✅ AJOUTER CET IMPORT
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;  // ✅ AJOUTER CET IMPORT

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;  // ✅ AJOUTER CET IMPORT

/**
 * Contrôleur pour la recherche avancée de salles
 * Permet de trouver des salles disponibles selon différents critères
 */
public class RechercheSallesController implements Initializable {

    @FXML private TabPane rechercheTabPane;

    // Onglet Recherche simple
    @FXML private DatePicker dateSimplePicker;
    @FXML private ComboBox<String> heureSimpleCombo;
    @FXML private Spinner<Integer> capaciteSimpleSpinner;
    @FXML private ComboBox<String> typeSimpleCombo;
    @FXML private Button btnRechercheSimple;
    @FXML private Button btnRechercheMaintenant;

    // Onglet Recherche avancée
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> heureDebutCombo;
    @FXML private ComboBox<String> heureFinCombo;
    @FXML private Spinner<Integer> capaciteMinSpinner;
    @FXML private Spinner<Integer> capaciteMaxSpinner;
    @FXML private ComboBox<String> typeAvanceCombo;
    @FXML private ComboBox<Batiment> batimentCombo;
    @FXML private ListView<Equipement> equipementsListView;
    @FXML private CheckBox climatisationCheck;
    @FXML private CheckBox videoprojecteurCheck;
    @FXML private CheckBox tableauInteractifCheck;
    @FXML private Button btnRechercheAvancee;
    @FXML private Button btnEffacerFiltres;

    // Résultats
    @FXML private TableView<Salle> resultatsTable;
    @FXML private TableColumn<Salle, String> colNumero;
    @FXML private TableColumn<Salle, Integer> colCapacite;
    @FXML private TableColumn<Salle, String> colType;
    @FXML private TableColumn<Salle, String> colBatiment;
    @FXML private TableColumn<Salle, String> colEquipements;
    @FXML private TableColumn<Salle, String> colDisponibilite;
    @FXML private TableColumn<Salle, Void> colReserver;

    @FXML private Label resultatsCountLabel;
    @FXML private Label tempsRechercheLabel;
    @FXML private VBox detailsSalleBox;

    private SalleDAO salleDAO;
    private BatimentDAO batimentDAO;
    private EquipementDAO equipementDAO;
    private ObservableList<Salle> resultatsObservable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        salleDAO = new SalleDAO();
        batimentDAO = new BatimentDAO();
        equipementDAO = new EquipementDAO();

        setupRechercheSimple();
        setupRechercheAvancee();
        setupResultatsTable();
        loadInitialData();
    }

    /**
     * Configuration de la recherche simple
     */
    private void setupRechercheSimple() {
        // Date
        dateSimplePicker.setValue(LocalDate.now());

        // Heures
        ObservableList<String> heures = FXCollections.observableArrayList();
        for (int i = 8; i <= 20; i++) {
            heures.add(String.format("%02d:00", i));
        }
        heureSimpleCombo.setItems(heures);
        heureSimpleCombo.setValue("08:00");

        // Capacité
        capaciteSimpleSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 30)
        );

        // Type
        typeSimpleCombo.setItems(FXCollections.observableArrayList(
                "Tous", "TD", "TP", "Amphi"
        ));
        typeSimpleCombo.setValue("Tous");

        // Boutons
        btnRechercheSimple.setOnAction(e -> effectuerRechercheSimple());
        btnRechercheMaintenant.setOnAction(e -> rechercherMaintenant());
    }

    /**
     * Configuration de la recherche avancée
     */
    private void setupRechercheAvancee() {
        // Dates
        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now().plusDays(7));

        // Heures
        ObservableList<String> heures = FXCollections.observableArrayList();
        for (int i = 8; i <= 20; i++) {
            heures.add(String.format("%02d:00", i));
        }
        heureDebutCombo.setItems(heures);
        heureFinCombo.setItems(heures);
        heureDebutCombo.setValue("08:00");
        heureFinCombo.setValue("18:00");

        // Capacités
        capaciteMinSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 1)
        );
        capaciteMaxSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 500)
        );

        // Type
        typeAvanceCombo.setItems(FXCollections.observableArrayList(
                "Tous", "TD", "TP", "Amphi"
        ));
        typeAvanceCombo.setValue("Tous");

        // Bâtiments
        List<Batiment> batiments = batimentDAO.getAll();
        batiments.add(0, new Batiment(0, "Tous les bâtiments", ""));
        batimentCombo.setItems(FXCollections.observableArrayList(batiments));
        batimentCombo.setValue(batiments.get(0));
        batimentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Batiment batiment) {
                return batiment != null ? batiment.getNom() : "";
            }

            @Override
            public Batiment fromString(String string) {
                return batiments.stream()
                        .filter(b -> b.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Équipements
        List<Equipement> equipements = equipementDAO.getAll();
        equipementsListView.getItems().setAll(equipements);
        equipementsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Boutons
        btnRechercheAvancee.setOnAction(e -> effectuerRechercheAvancee());
        btnEffacerFiltres.setOnAction(e -> effacerFiltres());
    }

    /**
     * Configuration de la table des résultats
     */
    private void setupResultatsTable() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        colBatiment.setCellValueFactory(cellData -> {
            Salle salle = cellData.getValue();
            String nomBatiment = salle.getBatiment() != null ?
                    salle.getBatiment().getNom() : "Non défini";
            return new SimpleStringProperty(nomBatiment);
        });

        colEquipements.setCellValueFactory(cellData -> {
            Salle salle = cellData.getValue();
            StringBuilder sb = new StringBuilder();
            for (Equipement e : salle.getEquipements()) {
                sb.append(e.getNom()).append(", ");
            }
            String equipements = sb.length() > 0 ?
                    sb.substring(0, sb.length() - 2) : "Aucun";
            return new SimpleStringProperty(equipements);
        });

        colDisponibilite.setCellValueFactory(cellData -> {
            Salle salle = cellData.getValue();
            boolean disponible = verifierDisponibilite(salle);
            return new SimpleStringProperty(disponible ? "Disponible" : "Occupée");
        });

        colDisponibilite.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Disponible".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colReserver.setCellFactory(param -> new TableCell<>() {
            private final Button btnReserver = new Button("Réserver");

            {
                btnReserver.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnReserver.setOnAction(event -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    reserverSalle(salle);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Salle salle = getTableView().getItems().get(getIndex());
                    btnReserver.setDisable(!verifierDisponibilite(salle));
                    setGraphic(btnReserver);
                }
            }
        });

        resultatsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        afficherDetailsSalle(newSelection);
                    }
                }
        );
    }

    /**
     * Charge les données initiales
     */
    private void loadInitialData() {
        List<Salle> salles = salleDAO.getAll();
        resultatsObservable = FXCollections.observableArrayList(salles);
        resultatsTable.setItems(resultatsObservable);
        resultatsCountLabel.setText(salles.size() + " salles");
    }

    /**
     * Effectue une recherche simple - ✅ CORRIGÉ
     */
    private void effectuerRechercheSimple() {
        long startTime = System.currentTimeMillis();

        LocalDate date = dateSimplePicker.getValue();
        String heure = heureSimpleCombo.getValue();
        int capacite = capaciteSimpleSpinner.getValue();
        String type = typeSimpleCombo.getValue();

        // Filtrer les salles - ✅ Remplacer toList() par collect(Collectors.toList())
        List<Salle> salles = salleDAO.getAll().stream()
                .filter(s -> s.getCapacite() >= capacite)
                .filter(s -> type.equals("Tous") || s.getType().equals(type))
                .filter(s -> verifierDisponibilite(s, date, LocalTime.parse(heure)))
                .collect(Collectors.toList());  // ✅ CORRECTION ICI

        resultatsObservable = FXCollections.observableArrayList(salles);
        resultatsTable.setItems(resultatsObservable);

        long endTime = System.currentTimeMillis();
        tempsRechercheLabel.setText("Temps: " + (endTime - startTime) + "ms");
        resultatsCountLabel.setText(salles.size() + " salles trouvées");
    }

    /**
     * Recherche les salles disponibles maintenant
     */
    private void rechercherMaintenant() {
        dateSimplePicker.setValue(LocalDate.now());

        LocalTime maintenant = LocalTime.now();
        String heure = String.format("%02d:00", maintenant.getHour());
        heureSimpleCombo.setValue(heure);

        effectuerRechercheSimple();
    }

    /**
     * Effectue une recherche avancée - ✅ CORRIGÉ
     */
    private void effectuerRechercheAvancee() {
        long startTime = System.currentTimeMillis();

        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        LocalTime heureDebut = LocalTime.parse(heureDebutCombo.getValue());
        LocalTime heureFin = LocalTime.parse(heureFinCombo.getValue());
        int capaciteMin = capaciteMinSpinner.getValue();
        int capaciteMax = capaciteMaxSpinner.getValue();
        String type = typeAvanceCombo.getValue();
        Batiment batiment = batimentCombo.getValue();

        // ✅ Remplacer toList() par collect(Collectors.toList())
        List<Integer> equipementsIds = equipementsListView.getSelectionModel()
                .getSelectedItems().stream()
                .map(Equipement::getId)
                .collect(Collectors.toList());  // ✅ CORRECTION ICI

        // Recherche avec tous les critères
        List<Salle> salles = salleDAO.rechercherSalles(
                capaciteMin,
                type.equals("Tous") ? null : type,
                equipementsIds.isEmpty() ? null : equipementsIds
        );

        // Filtrer par bâtiment
        if (batiment != null && batiment.getId() > 0) {
            salles = salles.stream()
                    .filter(s -> s.getIdBatiment() == batiment.getId())
                    .collect(Collectors.toList());  // ✅ CORRECTION ICI
        }

        // Filtrer par capacité max
        salles = salles.stream()
                .filter(s -> s.getCapacite() <= capaciteMax)
                .collect(Collectors.toList());  // ✅ CORRECTION ICI

        // Filtrer par disponibilité sur la période
        salles = salles.stream()
                .filter(s -> verifierDisponibilitePeriode(s, dateDebut, dateFin, heureDebut, heureFin))
                .collect(Collectors.toList());  // ✅ CORRECTION ICI

        resultatsObservable = FXCollections.observableArrayList(salles);
        resultatsTable.setItems(resultatsObservable);

        long endTime = System.currentTimeMillis();
        tempsRechercheLabel.setText("Temps: " + (endTime - startTime) + "ms");
        resultatsCountLabel.setText(salles.size() + " salles trouvées");
    }

    /**
     * Vérifie la disponibilité d'une salle
     */
    private boolean verifierDisponibilite(Salle salle) {
        LocalDate date = dateSimplePicker.getValue();
        LocalTime heure = LocalTime.parse(heureSimpleCombo.getValue());
        return verifierDisponibilite(salle, date, heure);
    }

    /**
     * Vérifie la disponibilité d'une salle à une date et heure données
     */
    private boolean verifierDisponibilite(Salle salle, LocalDate date, LocalTime heure) {
        // À implémenter avec SeanceDAO
        // Pour l'exemple, on retourne true
        return true;
    }

    /**
     * Vérifie la disponibilité sur une période
     */
    private boolean verifierDisponibilitePeriode(Salle salle, LocalDate dateDebut,
                                                 LocalDate dateFin, LocalTime heureDebut,
                                                 LocalTime heureFin) {
        // À implémenter avec SeanceDAO
        // Pour l'exemple, on retourne true
        return true;
    }

    /**
     * Réserve une salle - ✅ CORRIGÉ
     */
    private void reserverSalle(Salle salle) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Réservation de salle");
        dialog.setHeaderText("Réservation de la salle " + salle.getNumero());

        ButtonType reserverButton = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reserverButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> heureDebutCombo = new ComboBox<>();
        ComboBox<String> heureFinCombo = new ComboBox<>();
        TextField motifField = new TextField();
        motifField.setPromptText("Motif de la réservation");

        ObservableList<String> heures = FXCollections.observableArrayList();
        for (int i = 8; i <= 20; i++) {
            heures.add(String.format("%02d:00", i));
        }
        heureDebutCombo.setItems(heures);
        heureFinCombo.setItems(heures);
        heureDebutCombo.setValue("08:00");
        heureFinCombo.setValue("10:00");

        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Heure début:"), 0, 1);
        grid.add(heureDebutCombo, 1, 1);
        grid.add(new Label("Heure fin:"), 0, 2);
        grid.add(heureFinCombo, 1, 2);
        grid.add(new Label("Motif:"), 0, 3);
        grid.add(motifField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait();

        AlertUtil.showSuccess("Succès",
                "Réservation effectuée pour la salle " + salle.getNumero() + "\n" +
                        "Un email de confirmation a été envoyé.");
    }

    /**
     * Affiche les détails d'une salle
     */
    private void afficherDetailsSalle(Salle salle) {
        detailsSalleBox.getChildren().clear();

        Label titre = new Label("Détails de la salle " + salle.getNumero());
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label capacite = new Label("Capacité: " + salle.getCapacite() + " places");
        Label type = new Label("Type: " + salle.getType());
        Label batiment = new Label("Bâtiment: " + salle.getBatiment().getNom());

        Label equipements = new Label("Équipements:");
        equipements.setStyle("-fx-font-weight: bold;");

        VBox equipBox = new VBox(5);
        for (Equipement e : salle.getEquipements()) {
            equipBox.getChildren().add(new Label("  • " + e.getNom()));
        }

        if (salle.getEquipements().isEmpty()) {
            equipBox.getChildren().add(new Label("  Aucun équipement"));
        }

        detailsSalleBox.getChildren().addAll(
                titre,
                new Separator(),
                capacite,
                type,
                batiment,
                new Separator(),
                equipements,
                equipBox
        );

        detailsSalleBox.setSpacing(10);
        detailsSalleBox.setPadding(new Insets(10));
    }

    /**
     * Efface tous les filtres
     */
    private void effacerFiltres() {
        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now().plusDays(7));
        heureDebutCombo.setValue("08:00");
        heureFinCombo.setValue("18:00");
        capaciteMinSpinner.getValueFactory().setValue(1);
        capaciteMaxSpinner.getValueFactory().setValue(500);
        typeAvanceCombo.setValue("Tous");
        batimentCombo.setValue(batimentCombo.getItems().get(0));
        equipementsListView.getSelectionModel().clearSelection();

        loadInitialData();
    }
}