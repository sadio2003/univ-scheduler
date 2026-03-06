package com.univ.scheduler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;  // ✅ AJOUTER CET IMPORT

/**
 * Classe représentant une salle de cours
 * Peut être de type TD, TP ou Amphi
 */
public class Salle {
    private int id;
    private String numero;
    private int capacite;
    private String type; // "TD", "TP", "Amphi"
    private int idBatiment;

    // Objets associés (pour les jointures)
    private Batiment batiment;
    private List<Equipement> equipements = new ArrayList<>();

    // Constructeurs
    public Salle() {}

    public Salle(int id, String numero, int capacite, String type, int idBatiment) {
        this.id = id;
        this.numero = numero;
        this.capacite = capacite;
        this.type = type;
        this.idBatiment = idBatiment;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getIdBatiment() { return idBatiment; }
    public void setIdBatiment(int idBatiment) { this.idBatiment = idBatiment; }

    public Batiment getBatiment() { return batiment; }
    public void setBatiment(Batiment batiment) {
        this.batiment = batiment;
        if (batiment != null) {
            this.idBatiment = batiment.getId();
        }
    }

    public List<Equipement> getEquipements() { return equipements; }
    public void setEquipements(List<Equipement> equipements) { this.equipements = equipements; }

    /**
     * Ajoute un équipement à la salle
     */
    public void addEquipement(Equipement equipement) {
        if (!this.equipements.contains(equipement)) {
            this.equipements.add(equipement);
        }
    }

    /**
     * Supprime un équipement de la salle
     */
    public void removeEquipement(Equipement equipement) {
        this.equipements.remove(equipement);
    }

    /**
     * Vérifie si la salle dispose d'un équipement spécifique
     */
    public boolean hasEquipement(String nomEquipement) {
        return equipements.stream()
                .anyMatch(e -> e.getNom().equalsIgnoreCase(nomEquipement));
    }

    /**
     * Vérifie si la salle est de type TD
     */
    public boolean isTD() {
        return "TD".equals(type);
    }

    /**
     * Vérifie si la salle est de type TP
     */
    public boolean isTP() {
        return "TP".equals(type);
    }

    /**
     * Vérifie si la salle est un amphithéâtre
     */
    public boolean isAmphi() {
        return "Amphi".equals(type);
    }

    /**
     * Obtient la liste des noms des équipements - ✅ CORRIGÉ POUR JAVA 11
     */
    public List<String> getEquipementsNoms() {
        return equipements.stream()
                .map(Equipement::getNom)
                .collect(Collectors.toList());  // ✅ Remplacer toList() par collect(Collectors.toList())
    }

    /**
     * Obtient une description textuelle des équipements
     */
    public String getEquipementsDescription() {
        if (equipements.isEmpty()) {
            return "Aucun équipement";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < equipements.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(equipements.get(i).getNom());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return numero + " (" + capacite + " places, " + type + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Salle salle = (Salle) obj;
        return id == salle.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}