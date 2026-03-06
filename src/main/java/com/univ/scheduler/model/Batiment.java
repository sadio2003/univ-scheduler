package com.univ.scheduler.model;

/**
 * Classe représentant un bâtiment de l'université
 * Contient plusieurs salles
 */
public class Batiment {
    private int id;
    private String nom;
    private String localisation;

    // Constructeurs
    public Batiment() {}

    public Batiment(int id, String nom, String localisation) {
        this.id = id;
        this.nom = nom;
        this.localisation = localisation;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Batiment batiment = (Batiment) obj;
        return id == batiment.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}