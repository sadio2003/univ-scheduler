package com.univ.scheduler.model;

/**
 * Classe représentant un équipement disponible dans une salle
 * Ex: Vidéoprojecteur, Tableau interactif, Climatisation
 */
public class Equipement {
    private int id;
    private String nom;

    // Constructeurs
    public Equipement() {}

    public Equipement(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Equipement that = (Equipement) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}