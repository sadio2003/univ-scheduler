package com.univ.scheduler.model;

/**
 * Classe représentant un utilisateur du système
 * Peut être Admin, Gestionnaire, Enseignant ou Etudiant
 */
public class Utilisateur {
    private int id;
    private String nom;
    private String email;
    private String motDePasse;
    private String role; // "Admin", "Gestionnaire", "Enseignant", "Etudiant"

    // Constructeurs
    public Utilisateur() {}

    public Utilisateur(int id, String nom, String email, String role) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
    }

    public Utilisateur(int id, String nom, String email, String motDePasse, String role) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /**
     * Vérifie si l'utilisateur est un administrateur
     */
    public boolean isAdmin() {
        return "Admin".equals(role);
    }

    /**
     * Vérifie si l'utilisateur est un gestionnaire
     */
    public boolean isGestionnaire() {
        return "Gestionnaire".equals(role);
    }

    /**
     * Vérifie si l'utilisateur est un enseignant
     */
    public boolean isEnseignant() {
        return "Enseignant".equals(role);
    }

    /**
     * Vérifie si l'utilisateur est un étudiant
     */
    public boolean isEtudiant() {
        return "Etudiant".equals(role);
    }

    @Override
    public String toString() {
        return nom + " (" + role + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Utilisateur that = (Utilisateur) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}