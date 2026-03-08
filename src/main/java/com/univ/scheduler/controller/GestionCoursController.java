package com.univ.scheduler.controller;

import com.univ.scheduler.db.CoursDAO;
import com.univ.scheduler.db.UtilisateurDAO;
import com.univ.scheduler.model.Cours;           // ✅ AJOUTÉ
import com.univ.scheduler.model.Utilisateur;     // ✅ AJOUTÉ
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
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des cours
 * Gère les matières et leur affectation aux enseignants
 */
public class GestionCoursController implements Initializable {

    @FXML private TableView<Cours> tableCours;
    @FXML private TableColumn<Cours, Integer> colId;
    @FXML private TableColumn<Cours, String> colMatiere;
    @FXML private TableColumn<Cours, String> colEnseignant;
    @FXML private TableColumn<Cours, String> colClasse;
    @FXML private TableColumn<Cours, String> colGroupe;
    @FXML private TableColumn<Cours, Void> colActions;

    @FXML private TextField txtMatiere;
    @FXML private ComboBox<Utilisateur> cmbEnseignant;
    @FXML private ComboBox<String> cmbClasse;
    @FXML private ComboBox<String> cmbGroupe;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtreClasse;
    @FXML private ComboBox<String> filtreEnseignant;

    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnExporter;

    @FXML private Label totalCoursLabel;
    @FXML private Label totalEnseignantsLabel;
    @FXML private Label coursParClasseLabel;

    private CoursDAO coursDAO;
    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Cours> coursObservable;
    private Cours coursEnCours;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coursDAO = new CoursDAO();
        utilisateurDAO = new UtilisateurDAO();

        setupTable();
        setupCombos();
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
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));

        colEnseignant.setCellValueFactory(cellData -> {
            Cours cours = cellData.getValue();
            String nomEnseignant = cours.getEnseignant() != null ?
                    cours.getEnseignant().getNom() : "Non assigné";
            return new SimpleStringProperty(nomEnseignant);
        });

        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colGroupe.setCellValueFactory(new PropertyValueFactory<>("groupe"));

        // Colonne d'actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final Button btnEmploi = new Button("📅");

            {
                btnEdit.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    editCours(cours);
                });

                btnDelete.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    deleteCours(cours);
                });

                btnEmploi.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    showEmploiDuTemps(cours);
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
                    pane.add(btnEmploi, 2, 0);
                    setGraphic(pane);
                }
            }
        });

        tableCours.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        fillForm(newSelection);
                    }
                }
        );
    }

    /**
     * Configuration des combobox
     */
    private void setupCombos() {
        // Classes disponibles
        ObservableList<String> classes = FXCollections.observableArrayList(
                "LI1", "LI2", "LI3", "GI1", "GI2", "GI3", "MI", "M1", "M2"
        );
        cmbClasse.setItems(classes);
        filtreClasse.setItems(classes);
        filtreClasse.getItems().add(0, "Toutes");

        // Groupes
        ObservableList<String> groupes = FXCollections.observableArrayList(
                "G1", "G2", "G3", "G4", "A", "B", "C"
        );
        cmbGroupe.setItems(groupes);

        // Enseignants
        List<Utilisateur> enseignants = utilisateurDAO.getAll().stream()
                .filter(u -> "Enseignant".equals(u.getRole()))
                .collect(Collectors.toList());

        cmbEnseignant.setItems(FXCollections.observableArrayList(enseignants));

        // Filtre enseignant
        ObservableList<String> nomsEnseignants = FXCollections.observableArrayList();
        nomsEnseignants.add("Tous");
        enseignants.forEach(e -> nomsEnseignants.add(e.getNom()));
        filtreEnseignant.setItems(nomsEnseignants);

        filtreClasse.setValue("Toutes");
        filtreEnseignant.setValue("Tous");

        filtreClasse.setOnAction(e -> applyFilters());
        filtreEnseignant.setOnAction(e -> applyFilters());
    }

    /**
     * Configuration du formulaire
     */
    private void setupForm() {
        btnAjouter.setOnAction(e -> ajouterCours());
        btnModifier.setOnAction(e -> modifierCours());
        btnSupprimer.setOnAction(e -> supprimerCours());
        btnAnnuler.setOnAction(e -> resetForm());
        btnExporter.setOnAction(e -> exporterCours());

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    /**
     * Charge les données
     */
    private void loadData() {
        List<Cours> coursList = coursDAO.getAll();
        coursObservable = FXCollections.observableArrayList(coursList);
        tableCours.setItems(coursObservable);
    }

    /**
     * Configure la recherche
     */
    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            applyFilters();
        });
    }

    /**
     * Applique les filtres
     */
    private void applyFilters() {
        String search = searchField.getText().toLowerCase();
        String classe = filtreClasse.getValue();
        String enseignant = filtreEnseignant.getValue();

        ObservableList<Cours> filtered = FXCollections.observableArrayList(
                coursObservable.filtered(cours -> {
                    // Filtre recherche texte
                    boolean matchSearch = search.isEmpty() ||
                            cours.getNomMatiere().toLowerCase().contains(search) ||
                            (cours.getEnseignant() != null &&
                                    cours.getEnseignant().getNom().toLowerCase().contains(search));

                    // Filtre classe
                    boolean matchClasse = "Toutes".equals(classe) ||
                            classe.equals(cours.getClasse());

                    // Filtre enseignant
                    boolean matchEnseignant = "Tous".equals(enseignant) ||
                            (cours.getEnseignant() != null &&
                                    enseignant.equals(cours.getEnseignant().getNom()));

                    return matchSearch && matchClasse && matchEnseignant;
                })
        );

        tableCours.setItems(filtered);
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistics() {
        List<Cours> coursList = coursDAO.getAll();
        totalCoursLabel.setText(String.valueOf(coursList.size()));

        long nbEnseignants = coursList.stream()
                .map(Cours::getIdEnseignant)
                .distinct()
                .count();
        totalEnseignantsLabel.setText(String.valueOf(nbEnseignants));

        // Moyenne de cours par classe
        coursParClasseLabel.setText(String.format("%.1f",
                coursList.size() / 8.0)); // 8 classes environ
    }

    /**
     * Remplit le formulaire
     */
    private void fillForm(Cours cours) {
        this.coursEnCours = cours;
        txtMatiere.setText(cours.getNomMatiere());
        cmbEnseignant.setValue(cours.getEnseignant());
        cmbClasse.setValue(cours.getClasse());
        cmbGroupe.setValue(cours.getGroupe());

        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
        btnAjouter.setDisable(true);
    }

    /**
     * Ajoute un cours
     */
    private void ajouterCours() {
        if (!validateForm()) return;

        Cours cours = new Cours();
        cours.setNomMatiere(txtMatiere.getText().trim());
        cours.setIdEnseignant(cmbEnseignant.getValue().getId());
        cours.setEnseignant(cmbEnseignant.getValue());
        cours.setClasse(cmbClasse.getValue());
        cours.setGroupe(cmbGroupe.getValue());

        if (coursDAO.insert(cours)) {
            AlertUtil.showSuccess("Succès", "Cours ajouté avec succès");
            loadData();
            resetForm();
            updateStatistics();
        } else {
            AlertUtil.showError("Erreur", "Impossible d'ajouter le cours", "");
        }
    }

    /**
     * Modifie un cours
     */
    private void modifierCours() {
        if (coursEnCours == null) {
            AlertUtil.showWarning("Attention", "Sélectionnez un cours à modifier");
            return;
        }

        if (!validateForm()) return;

        coursEnCours.setNomMatiere(txtMatiere.getText().trim());
        coursEnCours.setIdEnseignant(cmbEnseignant.getValue().getId());
        coursEnCours.setEnseignant(cmbEnseignant.getValue());
        coursEnCours.setClasse(cmbClasse.getValue());
        coursEnCours.setGroupe(cmbGroupe.getValue());

        if (coursDAO.update(coursEnCours)) {
            AlertUtil.showSuccess("Succès", "Cours modifié avec succès");
            loadData();
            resetForm();
            updateStatistics();
        } else {
            AlertUtil.showError("Erreur", "Impossible de modifier le cours", "");
        }
    }

    /**
     * Édite un cours
     */
    private void editCours(Cours cours) {
        fillForm(cours);
    }

    /**
     * Supprime un cours
     */
    private void supprimerCours() {
        if (coursEnCours == null) {
            AlertUtil.showWarning("Attention", "Sélectionnez un cours à supprimer");
            return;
        }

        deleteCours(coursEnCours);
    }

    /**
     * Supprime un cours
     */
    private void deleteCours(Cours cours) {
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation",
                "Voulez-vous vraiment supprimer le cours " + cours.getNomMatiere() + " ?"
        );

        if (confirm) {
            if (coursDAO.delete(cours.getId())) {
                AlertUtil.showSuccess("Succès", "Cours supprimé");
                loadData();
                resetForm();
                updateStatistics();
            } else {
                AlertUtil.showError("Erreur",
                        "Impossible de supprimer le cours",
                        "Ce cours est peut-être utilisé dans des séances");
            }
        }
    }

    /**
     * Affiche l'emploi du temps d'un cours
     */
    private void showEmploiDuTemps(Cours cours) {
        AlertUtil.showInformation("Emploi du temps",
                "Affichage de l'emploi du temps pour : " + cours.getNomMatiere() + "\n" +
                        "À implémenter avec la vue EmploiDuTemps");
    }

    /**
     * Valide le formulaire
     */
    private boolean validateForm() {
        if (txtMatiere.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le nom de la matière est requis");
            txtMatiere.requestFocus();
            return false;
        }

        if (cmbEnseignant.getValue() == null) {
            AlertUtil.showWarning("Validation", "L'enseignant est requis");
            cmbEnseignant.requestFocus();
            return false;
        }

        if (cmbClasse.getValue() == null) {
            AlertUtil.showWarning("Validation", "La classe est requise");
            cmbClasse.requestFocus();
            return false;
        }

        if (cmbGroupe.getValue() == null) {
            AlertUtil.showWarning("Validation", "Le groupe est requis");
            cmbGroupe.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Réinitialise le formulaire
     */
    private void resetForm() {
        coursEnCours = null;
        txtMatiere.clear();
        cmbEnseignant.setValue(null);
        cmbClasse.setValue(null);
        cmbGroupe.setValue(null);

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnAjouter.setDisable(false);

        tableCours.getSelectionModel().clearSelection();
    }

    /**
     * Exporte les cours
     */
    private void exporterCours() {
        AlertUtil.showInformation("Export",
                "Export de " + coursObservable.size() + " cours\n" +
                        "Format: PDF/Excel\n" +
                        "À implémenter");
    }
}