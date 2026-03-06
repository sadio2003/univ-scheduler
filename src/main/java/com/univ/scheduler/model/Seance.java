package com.univ.scheduler.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe représentant une séance de cours planifiée
 * Associe un cours, une salle, un jour et un horaire
 */
public class Seance {
    private int id;
    private int idCours;
    private int idSalle;
    private String jourSemaine; // "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"
    private LocalTime heureDebut;
    private int duree; // en minutes

    // Objets associés
    private Cours cours;
    private Salle salle;

    // Constructeurs
    public Seance() {}

    public Seance(int id, int idCours, int idSalle, String jourSemaine,
                  LocalTime heureDebut, int duree) {
        this.id = id;
        this.idCours = idCours;
        this.idSalle = idSalle;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.duree = duree;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdCours() { return idCours; }
    public void setIdCours(int idCours) {
        this.idCours = idCours;
    }

    public Cours getCours() { return cours; }
    public void setCours(Cours cours) {
        this.cours = cours;
        if (cours != null) {
            this.idCours = cours.getId();
        }
    }

    public int getIdSalle() { return idSalle; }
    public void setIdSalle(int idSalle) {
        this.idSalle = idSalle;
    }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) {
        this.salle = salle;
        if (salle != null) {
            this.idSalle = salle.getId();
        }
    }

    public String getJourSemaine() { return jourSemaine; }
    public void setJourSemaine(String jourSemaine) {
        this.jourSemaine = jourSemaine != null ? jourSemaine.toUpperCase() : null;
    }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    /**
     * Calcule l'heure de fin de la séance
     */
    public LocalTime getHeureFin() {
        return heureDebut != null ? heureDebut.plusMinutes(duree) : null;
    }

    /**
     * Formate l'horaire (ex: 08:00 - 10:00)
     */
    public String getHoraireFormat() {
        if (heureDebut == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return heureDebut.format(formatter) + " - " + getHeureFin().format(formatter);
    }

    /**
     * Vérifie si la séance a lieu le matin (avant 12h)
     */
    public boolean isMatin() {
        return heureDebut != null && heureDebut.getHour() < 12;
    }

    /**
     * Vérifie si la séance a lieu l'après-midi (après 12h)
     */
    public boolean isApresMidi() {
        return heureDebut != null && heureDebut.getHour() >= 12;
    }

    /**
     * Convertit le jour en français - ✅ VERSION JAVA 11
     */
    public String getJourFrancais() {
        if (jourSemaine == null) return "";

        // ✅ Switch traditionnel compatible Java 11
        switch (jourSemaine) {
            case "LUNDI":
                return "Lundi";
            case "MARDI":
                return "Mardi";
            case "MERCREDI":
                return "Mercredi";
            case "JEUDI":
                return "Jeudi";
            case "VENDREDI":
                return "Vendredi";
            case "SAMEDI":
                return "Samedi";
            case "DIMANCHE":
                return "Dimanche";
            default:
                return jourSemaine;
        }
    }

    @Override
    public String toString() {
        return getJourFrancais() + " " + getHoraireFormat() + " - " +
                (cours != null ? cours.getNomMatiere() : "?") +
                " (" + (salle != null ? salle.getNumero() : "?") + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Seance seance = (Seance) obj;
        return id == seance.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}