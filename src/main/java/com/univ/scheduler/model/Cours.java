package com.univ.scheduler.model;

/**
 * Classe représentant un cours (matière)
 * Associé à un enseignant, une classe et un groupe
 */
public class Cours {
    private int id;
    private String nomMatiere;
    private int idEnseignant;
    private String classe; // "LI2", "GI1", etc.
    private String groupe; // "G1", "G2", etc. (peut être null)

    // Objets associés
    private Utilisateur enseignant;

    // Constructeurs
    public Cours() {}

    public Cours(int id, String nomMatiere, int idEnseignant, String classe, String groupe) {
        this.id = id;
        this.nomMatiere = nomMatiere;
        this.idEnseignant = idEnseignant;
        this.classe = classe;
        this.groupe = groupe;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomMatiere() { return nomMatiere; }
    public void setNomMatiere(String nomMatiere) { this.nomMatiere = nomMatiere; }

    public int getIdEnseignant() { return idEnseignant; }
    public void setIdEnseignant(int idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public Utilisateur getEnseignant() { return enseignant; }
    public void setEnseignant(Utilisateur enseignant) {
        this.enseignant = enseignant;
        if (enseignant != null) {
            this.idEnseignant = enseignant.getId();
        }
    }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    /**
     * Obtient le nom complet de la classe (avec groupe si existe)
     */
    public String getClasseComplete() {
        return groupe != null && !groupe.isEmpty()
                ? classe + " " + groupe
                : classe;
    }

    /**
     * Vérifie si le cours a un groupe
     */
    public boolean hasGroupe() {
        return groupe != null && !groupe.isEmpty();
    }

    @Override
    public String toString() {
        return nomMatiere + " - " + getClasseComplete();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cours cours = (Cours) obj;
        return id == cours.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}