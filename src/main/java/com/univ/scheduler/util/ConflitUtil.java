package com.univ.scheduler.util;

import javafx.collections.ObservableList;
import com.univ.scheduler.model.Seance;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire pour la détection des conflits dans l'emploi du temps
 * Vérifie les chevauchements de salles, enseignants, classes
 */
public class ConflitUtil {

    /**
     * Vérifie tous les types de conflits pour une liste de séances
     * @return Liste des messages de conflit
     */
    public static List<String> verifierTousConflits(ObservableList<Seance> seances) {
        List<String> conflits = new ArrayList<>();

        conflits.addAll(verifierConflitsSalles(seances));
        conflits.addAll(verifierConflitsEnseignants(seances));
        conflits.addAll(verifierConflitsClasses(seances));

        return conflits;
    }

    /**
     * Vérifie les conflits de salles (même salle au même moment)
     */
    public static List<String> verifierConflitsSalles(List<Seance> seances) {
        List<String> conflits = new ArrayList<>();

        for (int i = 0; i < seances.size(); i++) {
            Seance s1 = seances.get(i);
            for (int j = i + 1; j < seances.size(); j++) {
                Seance s2 = seances.get(j);

                if (s1.getSalle().getId() == s2.getSalle().getId() &&
                        s1.getJourSemaine().equals(s2.getJourSemaine()) &&
                        chevauchement(s1, s2)) {

                    conflits.add(String.format(
                            "❌ Conflit de salle: %s est réservée pour '%s' et '%s' le %s à %s",
                            s1.getSalle().getNumero(),
                            s1.getCours().getNomMatiere(),
                            s2.getCours().getNomMatiere(),
                            s1.getJourSemaine(),
                            DateUtil.formatTime(s1.getHeureDebut())
                    ));
                }
            }
        }

        return conflits;
    }

    /**
     * Vérifie les conflits d'enseignants (même enseignant au même moment)
     */
    public static List<String> verifierConflitsEnseignants(List<Seance> seances) {
        List<String> conflits = new ArrayList<>();

        for (int i = 0; i < seances.size(); i++) {
            Seance s1 = seances.get(i);
            for (int j = i + 1; j < seances.size(); j++) {
                Seance s2 = seances.get(j);

                if (s1.getCours().getIdEnseignant() == s2.getCours().getIdEnseignant() &&
                        s1.getJourSemaine().equals(s2.getJourSemaine()) &&
                        chevauchement(s1, s2)) {

                    conflits.add(String.format(
                            "❌ Conflit enseignant: %s doit enseigner '%s' et '%s' en même temps le %s",
                            s1.getCours().getEnseignant().getNom(),
                            s1.getCours().getNomMatiere(),
                            s2.getCours().getNomMatiere(),
                            s1.getJourSemaine()
                    ));
                }
            }
        }

        return conflits;
    }

    /**
     * Vérifie les conflits de classes (même classe au même moment)
     */
    public static List<String> verifierConflitsClasses(List<Seance> seances) {
        List<String> conflits = new ArrayList<>();

        for (int i = 0; i < seances.size(); i++) {
            Seance s1 = seances.get(i);
            for (int j = i + 1; j < seances.size(); j++) {
                Seance s2 = seances.get(j);

                if (s1.getCours().getClasse().equals(s2.getCours().getClasse()) &&
                        s1.getJourSemaine().equals(s2.getJourSemaine()) &&
                        chevauchement(s1, s2)) {

                    conflits.add(String.format(
                            "❌ Conflit classe: La classe %s a deux cours en même temps: '%s' et '%s' le %s",
                            s1.getCours().getClasse(),
                            s1.getCours().getNomMatiere(),
                            s2.getCours().getNomMatiere(),
                            s1.getJourSemaine()
                    ));
                }
            }
        }

        return conflits;
    }

    /**
     * Vérifie si une nouvelle séance est en conflit avec des séances existantes
     */
    public static boolean verifierConflit(Seance nouvelleSeance, List<Seance> seancesExistantes) {
        for (Seance existante : seancesExistantes) {
            // Même salle
            if (nouvelleSeance.getSalle().getId() == existante.getSalle().getId() &&
                    nouvelleSeance.getJourSemaine().equals(existante.getJourSemaine()) &&
                    chevauchement(nouvelleSeance, existante)) {
                return true;
            }

            // Même enseignant
            if (nouvelleSeance.getCours().getIdEnseignant() == existante.getCours().getIdEnseignant() &&
                    nouvelleSeance.getJourSemaine().equals(existante.getJourSemaine()) &&
                    chevauchement(nouvelleSeance, existante)) {
                return true;
            }

            // Même classe
            if (nouvelleSeance.getCours().getClasse().equals(existante.getCours().getClasse()) &&
                    nouvelleSeance.getJourSemaine().equals(existante.getJourSemaine()) &&
                    chevauchement(nouvelleSeance, existante)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Vérifie si deux séances se chevauchent dans le temps
     */
    private static boolean chevauchement(Seance s1, Seance s2) {
        return DateUtil.chevauchement(
                s1.getHeureDebut(), s1.getHeureFin(),
                s2.getHeureDebut(), s2.getHeureFin()
        );
    }

    /**
     * Suggère des créneaux disponibles pour une salle
     */
    public static List<String> suggererCreneauxDisponibles(String jour,
                                                           List<Seance> seancesExistantes) {
        List<String> suggestions = new ArrayList<>();

        // Créneaux standards
        String[] creneaux = {
                "08:00-10:00", "10:00-12:00", "14:00-16:00", "16:00-18:00"
        };

        for (String creneau : creneaux) {
            boolean disponible = true;

            for (Seance seance : seancesExistantes) {
                if (seance.getJourSemaine().equals(jour)) {
                    // Vérifier si le créneau est libre
                    // Logique à implémenter
                }
            }

            if (disponible) {
                suggestions.add(creneau);
            }
        }

        return suggestions;
    }

    /**
     * Calcule le taux d'occupation d'une salle
     */
    public static double calculerTauxOccupation(String salleNumero,
                                                List<Seance> seances,
                                                int totalCreneaux) {
        long seancesSalle = seances.stream()
                .filter(s -> s.getSalle().getNumero().equals(salleNumero))
                .count();

        return (double) seancesSalle / totalCreneaux * 100;
    }
}