package com.univ.scheduler.controller;

import com.univ.scheduler.model.Salle;
import com.univ.scheduler.model.Batiment;
import com.univ.scheduler.model.Equipement;
import com.univ.scheduler.db.EquipementDAO;  // Déjà utilisé mais import manquant
import com.univ.scheduler.db.BatimentDAO;
import com.univ.scheduler.db.SalleDAO;
import com.univ.scheduler.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;  // ✅ AJOUTER CET IMPORT

/**
 * Contrôleur pour la gestion des salles
 * CRUD complet avec recherche et filtres
 */
public class GestionSallesController implements Initializable {

    @FXML private TableView<Salle> tableSalles;
    @FXML private TableColumn<Salle, String> colNumero;
    @FXML private TableColumn<Salle, Integer> colCapacite;
    @FXML private TableColumn<Salle, String> colType;
    @FXML private TableColumn<Salle, String> colBatiment;
    @FXML private TableColumn<Salle, String> colEquipements;
    @FXML private TableColumn<Salle, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<Batiment> batimentFilter;
    @FXML private Spinner<Integer> capaciteMinFilter;
    @FXML private ListView<Equipement> equipementsList;

    @FXML private TextField txtNumero;
    @FXML private Spinner<Integer> txtCapacite;
    @FXML private ComboBox<String> cmbType;
    @FXML private ComboBox<Batiment> cmbBatiment;
    @FXML private ListView<Equipement> equipementsSelection;

    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnExporter;

    @FXML private Label totalSallesLabel;
    @FXML private Label capaciteMoyenneLabel;
    @FXML private ProgressBar occupationBar;

    private SalleDAO salleDAO;
    private BatimentDAO batimentDAO;
    private EquipementDAO equipementDAO;
    private ObservableList<Salle> sallesObservable;
    private Salle salleEnCours;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des DAO
        salleDAO = new SalleDAO();
        batimentDAO = new BatimentDAO();
        equipementDAO = new EquipementDAO();

        setupTable();
        setupFilters();
        setupForm();
        loadData();
        setupStatistics();
        setupSearch();
    }

    /**
     * Configuration de la table
     */
    private void setupTable() {
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

        // Colonne d'actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");

            {
                btnEdit.setOnAction(event -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    editSalle(salle);
                });

                btnDelete.setOnAction(event -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    deleteSalle(salle);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    GridPane pane = new GridPane();
                    pane.setHgap(5);
                    pane.add(btnEdit, 0, 0);
                    pane.add(btnDelete, 1, 0);
                    setGraphic(pane);
                }
            }
        });

        tableSalles.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        fillForm(newSelection);
                    }
                }
        );
    }

    /**
     * Configuration des filtres
     */
    private void setupFilters() {
        typeFilter.setItems(FXCollections.observableArrayList(
                "Tous", "TD", "TP", "Amphi"
        ));
        typeFilter.setValue("Tous");

        capaciteMinFilter.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500, 0)
        );

        typeFilter.setOnAction(e -> applyFilters());
        batimentFilter.setOnAction(e -> applyFilters());
        capaciteMinFilter.valueProperty().addListener((obs, old, val) -> applyFilters());

        // Charger les bâtiments pour le filtre
        List<Batiment> batiments = batimentDAO.getAll();
        batiments.add(0, new Batiment(0, "Tous les bâtiments", ""));
        batimentFilter.setItems(FXCollections.observableArrayList(batiments));
        batimentFilter.setValue(batiments.get(0));

        // Charger les équipements pour la sélection multiple
        List<Equipement> equipements = equipementDAO.getAll();
        equipementsList.getItems().setAll(equipements);
        equipementsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Configuration du formulaire
     */
    private void setupForm() {
        cmbType.setItems(FXCollections.observableArrayList("TD", "TP", "Amphi"));

        txtCapacite.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 30)
        );

        // Charger les bâtiments
        cmbBatiment.setItems(FXCollections.observableArrayList(batimentDAO.getAll()));

        // Charger les équipements pour la sélection multiple
        equipementsSelection.getItems().setAll(equipementDAO.getAll());
        equipementsSelection.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setupFormButtons();
    }

    /**
     * Configuration des boutons du formulaire
     */
    private void setupFormButtons() {
        btnAjouter.setOnAction(e -> ajouterSalle());
        btnModifier.setOnAction(e -> modifierSalle());
        btnSupprimer.setOnAction(e -> supprimerSalle());
        btnAnnuler.setOnAction(e -> resetForm());
        btnExporter.setOnAction(e -> exporterSalles());
    }

    /**
     * Charge les données dans la table
     */
    private void loadData() {
        List<Salle> salles = salleDAO.getAll();
        sallesObservable = FXCollections.observableArrayList(salles);
        tableSalles.setItems(sallesObservable);
    }

    /**
     * Configure les statistiques
     */
    private void setupStatistics() {
        updateStatistics();
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistics() {
        List<Salle> salles = salleDAO.getAll();
        totalSallesLabel.setText(String.valueOf(salles.size()));

        double moyenne = salles.stream()
                .mapToInt(Salle::getCapacite)
                .average()
                .orElse(0);
        capaciteMoyenneLabel.setText(String.format("%.1f", moyenne));

        // Taux d'occupation (simulé)
        occupationBar.setProgress(0.65);
    }

    /**
     * Configure la recherche
     */
    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val.isEmpty()) {
                tableSalles.setItems(sallesObservable);
            } else {
                ObservableList<Salle> filtered = FXCollections.observableArrayList(
                        sallesObservable.filtered(salle ->
                                salle.getNumero().toLowerCase().contains(val.toLowerCase()) ||
                                        salle.getType().toLowerCase().contains(val.toLowerCase())
                        )
                );
                tableSalles.setItems(filtered);
            }
        });
    }

    /**
     * Applique les filtres - ✅ CORRIGÉ POUR JAVA 11
     */
    private void applyFilters() {
        String type = typeFilter.getValue();
        Batiment batiment = batimentFilter.getValue();
        int capaciteMin = capaciteMinFilter.getValue();

        List<Equipement> selectedEquipements = equipementsList.getSelectionModel().getSelectedItems();
        List<Integer> equipementIds = selectedEquipements.stream()
                .map(Equipement::getId)
                .collect(Collectors.toList());  // ✅ Remplacer toList() par collect(Collectors.toList())

        List<Salle> filtered = salleDAO.rechercherSalles(
                capaciteMin > 0 ? capaciteMin : null,
                type.equals("Tous") ? null : type,
                equipementIds.isEmpty() ? null : equipementIds
        );

        tableSalles.setItems(FXCollections.observableArrayList(filtered));
    }

    /**
     * Remplit le formulaire avec une salle
     */
    private void fillForm(Salle salle) {
        this.salleEnCours = salle;
        txtNumero.setText(salle.getNumero());
        txtCapacite.getValueFactory().setValue(salle.getCapacite());
        cmbType.setValue(salle.getType());
        cmbBatiment.setValue(salle.getBatiment());

        // Sélectionner les équipements
        equipementsSelection.getSelectionModel().clearSelection();
        for (Equipement e : salle.getEquipements()) {
            equipementsSelection.getSelectionModel().select(e);
        }

        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
        btnAjouter.setDisable(true);
    }

    /**
     * Ajoute une nouvelle salle
     */
    private void ajouterSalle() {
        if (!validateForm()) return;

        Salle salle = new Salle();
        salle.setNumero(txtNumero.getText());
        salle.setCapacite(txtCapacite.getValue());
        salle.setType(cmbType.getValue());
        salle.setIdBatiment(cmbBatiment.getValue().getId());
        salle.setBatiment(cmbBatiment.getValue());

        List<Equipement> selectedEquipements = equipementsSelection.getSelectionModel().getSelectedItems();
        salle.setEquipements(selectedEquipements);

        if (salleDAO.insert(salle)) {
            AlertUtil.showSuccess("Succès", "Salle ajoutée avec succès");
            loadData();
            resetForm();
            updateStatistics();
        } else {
            AlertUtil.showError("Erreur", "Impossible d'ajouter la salle", "");
        }
    }

    /**
     * Modifie une salle existante
     */
    private void modifierSalle() {
        if (salleEnCours == null) {
            AlertUtil.showWarning("Attention", "Sélectionnez une salle à modifier");
            return;
        }

        if (!validateForm()) return;

        salleEnCours.setNumero(txtNumero.getText());
        salleEnCours.setCapacite(txtCapacite.getValue());
        salleEnCours.setType(cmbType.getValue());
        salleEnCours.setIdBatiment(cmbBatiment.getValue().getId());
        salleEnCours.setBatiment(cmbBatiment.getValue());

        List<Equipement> selectedEquipements = equipementsSelection.getSelectionModel().getSelectedItems();
        salleEnCours.setEquipements(selectedEquipements);

        if (salleDAO.update(salleEnCours)) {
            AlertUtil.showSuccess("Succès", "Salle modifiée avec succès");
            loadData();
            resetForm();
            updateStatistics();
        } else {
            AlertUtil.showError("Erreur", "Impossible de modifier la salle", "");
        }
    }

    /**
     * Édite une salle (depuis le bouton dans la table)
     */
    private void editSalle(Salle salle) {
        fillForm(salle);
    }

    /**
     * Supprime une salle (depuis le formulaire)
     */
    private void supprimerSalle() {
        if (salleEnCours == null) {
            AlertUtil.showWarning("Attention", "Sélectionnez une salle à supprimer");
            return;
        }

        deleteSalle(salleEnCours);
    }

    /**
     * Supprime une salle (depuis la table)
     */
    private void deleteSalle(Salle salle) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation",
                "Voulez-vous vraiment supprimer la salle " + salle.getNumero() + " ?"
        );

        if (confirm) {
            if (salleDAO.delete(salle.getId())) {
                AlertUtil.showSuccess("Succès", "Salle supprimée");
                loadData();
                resetForm();
                updateStatistics();
            } else {
                AlertUtil.showError("Erreur",
                        "Impossible de supprimer la salle",
                        "Cette salle est peut-être utilisée dans des séances");
            }
        }
    }

    /**
     * Valide le formulaire
     */
    private boolean validateForm() {
        if (txtNumero.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le numéro de salle est requis");
            txtNumero.requestFocus();
            return false;
        }

        if (cmbType.getValue() == null) {
            AlertUtil.showWarning("Validation", "Le type de salle est requis");
            cmbType.requestFocus();
            return false;
        }

        if (cmbBatiment.getValue() == null) {
            AlertUtil.showWarning("Validation", "Le bâtiment est requis");
            cmbBatiment.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Réinitialise le formulaire
     */
    private void resetForm() {
        salleEnCours = null;
        txtNumero.clear();
        txtCapacite.getValueFactory().setValue(30);
        cmbType.setValue(null);
        cmbBatiment.setValue(null);
        equipementsSelection.getSelectionModel().clearSelection();

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnAjouter.setDisable(false);

        tableSalles.getSelectionModel().clearSelection();
    }

    /**
     * Exporte les salles
     */
    private void exporterSalles() {
        // Implémenter l'export PDF/Excel
        AlertUtil.showInformation("Export",
                "Fonctionnalité d'export à implémenter\n" +
                        "Export des " + sallesObservable.size() + " salles");
    }
}