package com.univ.scheduler.controller;

import com.univ.scheduler.db.BatimentDAO;
import com.univ.scheduler.util.AlertUtil;
import com.univ.scheduler.model.Batiment;  // ✅ AJOUTER CET IMPORT
import javafx.beans.property.SimpleIntegerProperty;
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

/**
 * Contrôleur pour la gestion des bâtiments
 * Gère les bâtiments de l'université
 */
public class GestionBatimentsController implements Initializable {

    @FXML private TableView<Batiment> tableBatiments;
    @FXML private TableColumn<Batiment, Integer> colId;
    @FXML private TableColumn<Batiment, String> colNom;
    @FXML private TableColumn<Batiment, String> colLocalisation;
    @FXML private TableColumn<Batiment, Integer> colNombreSalles;
    @FXML private TableColumn<Batiment, Void> colActions;

    @FXML private TextField txtNom;
    @FXML private TextField txtLocalisation;
    @FXML private TextField searchField;

    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnExporter;

    @FXML private Label totalBatimentsLabel;
    @FXML private Label totalSallesLabel;
    @FXML private Label tauxOccupationLabel;

    private BatimentDAO batimentDAO;
    private ObservableList<Batiment> batimentsObservable;
    private Batiment batimentEnCours;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        batimentDAO = new BatimentDAO();

        setupTable();
        setupForm();
        loadData();
        setupSearch();
        updateStatistics();
    }

    /**
     * Configuration de la table
     */
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));

        colNombreSalles.setCellValueFactory(cellData -> {
            Batiment batiment = cellData.getValue();
            int nbSalles = batimentDAO.getNombreSalles(batiment.getId());
            return new SimpleIntegerProperty(nbSalles).asObject();
        });

        // Colonne d'actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final Button btnDetails = new Button("📋");

            {
                btnEdit.setOnAction(event -> {
                    Batiment batiment = getTableView().getItems().get(getIndex());
                    editBatiment(batiment);
                });

                btnDelete.setOnAction(event -> {
                    Batiment batiment = getTableView().getItems().get(getIndex());
                    deleteBatiment(batiment);
                });

                btnDetails.setOnAction(event -> {
                    Batiment batiment = getTableView().getItems().get(getIndex());
                    showDetails(batiment);
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
                    pane.add(btnDetails, 2, 0);
                    setGraphic(pane);
                }
            }
        });

        tableBatiments.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        fillForm(newSelection);
                    }
                }
        );
    }

    /**
     * Configuration du formulaire
     */
    private void setupForm() {
        btnAjouter.setOnAction(e -> ajouterBatiment());
        btnModifier.setOnAction(e -> modifierBatiment());
        btnSupprimer.setOnAction(e -> supprimerBatiment());
        btnAnnuler.setOnAction(e -> resetForm());
        btnExporter.setOnAction(e -> exporterBatiments());

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    /**
     * Charge les données
     */
    private void loadData() {
        List<Batiment> batiments = batimentDAO.getAll();
        batimentsObservable = FXCollections.observableArrayList(batiments);
        tableBatiments.setItems(batimentsObservable);
    }

    /**
     * Configure la recherche
     */
    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val.isEmpty()) {
                tableBatiments.setItems(batimentsObservable);
            } else {
                ObservableList<Batiment> filtered = FXCollections.observableArrayList(
                        batimentsObservable.filtered(batiment ->
                                batiment.getNom().toLowerCase().contains(val.toLowerCase()) ||
                                        batiment.getLocalisation().toLowerCase().contains(val.toLowerCase())
                        )
                );
                tableBatiments.setItems(filtered);
            }
        });
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistics() {
        List<Batiment> batiments = batimentDAO.getAll();
        totalBatimentsLabel.setText(String.valueOf(batiments.size()));

        int totalSalles = batiments.stream()
                .mapToInt(b -> batimentDAO.getNombreSalles(b.getId()))
                .sum();
        totalSallesLabel.setText(String.valueOf(totalSalles));

        // Taux d'occupation moyen (simulé)
        tauxOccupationLabel.setText("65%");
    }

    /**
     * Remplit le formulaire
     */
    private void fillForm(Batiment batiment) {
        this.batimentEnCours = batiment;
        txtNom.setText(batiment.getNom());
        txtLocalisation.setText(batiment.getLocalisation());

        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
        btnAjouter.setDisable(true);
    }

    /**
     * Ajoute un bâtiment
     */
    private void ajouterBatiment() {
        if (!validateForm()) return;

        Batiment batiment = new Batiment();
        batiment.setNom(txtNom.getText().trim());
        batiment.setLocalisation(txtLocalisation.getText().trim());

        if (batimentDAO.insert(batiment)) {
            AlertUtil.showSuccess("Succès", "Bâtiment ajouté avec succès");
            loadData();
            resetForm();
            updateStatistics();
        } else {
            AlertUtil.showError("Erreur", "Impossible d'ajouter le bâtiment", "");
        }
    }

    /**
     * Modifie un bâtiment
     */
    private void modifierBatiment() {
        if (batimentEnCours == null) {
            AlertUtil.showWarning("Attention", "Sélectionnez un bâtiment à modifier");
            return;
        }

        if (!validateForm()) return;

        batimentEnCours.setNom(txtNom.getText().trim());
        batimentEnCours.setLocalisation(txtLocalisation.getText().trim());

        if (batimentDAO.update(batimentEnCours)) {
            AlertUtil.showSuccess("Succès", "Bâtiment modifié avec succès");
            loadData();
            resetForm();
            updateStatistics();
        } else {
            AlertUtil.showError("Erreur", "Impossible de modifier le bâtiment", "");
        }
    }

    /**
     * Édite un bâtiment
     */
    private void editBatiment(Batiment batiment) {
        fillForm(batiment);
    }

    /**
     * Supprime un bâtiment
     */
    private void supprimerBatiment() {
        if (batimentEnCours == null) {
            AlertUtil.showWarning("Attention", "Sélectionnez un bâtiment à supprimer");
            return;
        }

        deleteBatiment(batimentEnCours);
    }

    /**
     * Supprime un bâtiment
     */
    private void deleteBatiment(Batiment batiment) {
        // Vérifier si le bâtiment contient des salles
        int nbSalles = batimentDAO.getNombreSalles(batiment.getId());

        if (nbSalles > 0) {
            AlertUtil.showWarning("Suppression impossible",
                    "Ce bâtiment contient " + nbSalles + " salle(s).\n" +
                            "Veuillez d'abord supprimer ou déplacer ces salles.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation",
                "Voulez-vous vraiment supprimer le bâtiment " + batiment.getNom() + " ?"
        );

        if (confirm) {
            if (batimentDAO.delete(batiment.getId())) {
                AlertUtil.showSuccess("Succès", "Bâtiment supprimé");
                loadData();
                resetForm();
                updateStatistics();
            } else {
                AlertUtil.showError("Erreur", "Impossible de supprimer le bâtiment", "");
            }
        }
    }

    /**
     * ✅ MÉTHODE CORRIGÉE - Plus de text blocks
     */
    private void showDetails(Batiment batiment) {
        int nbSalles = batimentDAO.getNombreSalles(batiment.getId());

        String message = String.format(
                "📋 DÉTAILS DU BÂTIMENT\n\n" +
                        "ID: %d\n" +
                        "Nom: %s\n" +
                        "Localisation: %s\n\n" +
                        "📊 STATISTIQUES\n" +
                        "Nombre de salles: %d\n" +
                        "Capacité totale: À calculer\n" +
                        "Taux d'occupation moyen: 65%%\n\n" +
                        "🏢 SALLES\n" +
                        "%s",
                batiment.getId(),
                batiment.getNom(),
                batiment.getLocalisation(),
                nbSalles,
                getSallesList(batiment.getId())
        );

        AlertUtil.showInformation("Détails du bâtiment", message);
    }

    /**
     * Obtient la liste des salles d'un bâtiment
     */
    private String getSallesList(int batimentId) {
        // À implémenter avec SalleDAO
        return "- Salle TD01 (30 places)\n- Salle TP02 (20 places)";
    }

    /**
     * Valide le formulaire
     */
    private boolean validateForm() {
        if (txtNom.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le nom du bâtiment est requis");
            txtNom.requestFocus();
            return false;
        }

        if (txtLocalisation.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validation", "La localisation est requise");
            txtLocalisation.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Réinitialise le formulaire
     */
    private void resetForm() {
        batimentEnCours = null;
        txtNom.clear();
        txtLocalisation.clear();

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnAjouter.setDisable(false);

        tableBatiments.getSelectionModel().clearSelection();
    }

    /**
     * Exporte les bâtiments
     */
    private void exporterBatiments() {
        StringBuilder export = new StringBuilder();
        export.append("ID;NOM;LOCALISATION;NOMBRE DE SALLES\n");

        for (Batiment b : batimentsObservable) {
            export.append(b.getId()).append(";")
                    .append(b.getNom()).append(";")
                    .append(b.getLocalisation()).append(";")
                    .append(batimentDAO.getNombreSalles(b.getId()))
                    .append("\n");
        }

        AlertUtil.showInformation("Export",
                "Export de " + batimentsObservable.size() + " bâtiments\n" +
                        "Format: CSV\n" +
                        "À implémenter: sauvegarde dans un fichier");
    }
}