package com.univ.scheduler.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utilitaire pour la manipulation des dates
 * Formatage, parsing, calculs
 */
public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_LONG =
            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER_LONG =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Formate une date au format dd/MM/yyyy
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Formate une date au format long (ex: lundi 15 janvier 2024)
     */
    public static String formatDateLong(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER_LONG) : "";
    }

    /**
     * Parse une date au format dd/MM/yyyy
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formate une heure au format HH:mm
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }

    /**
     * Formate une heure au format long HH:mm:ss
     */
    public static String formatTimeLong(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER_LONG) : "";
    }

    /**
     * Parse une heure au format HH:mm
     */
    public static LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convertit un jour en français
     */
    public static String getJourEnFrancais(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH));
    }

    /**
     * Convertit un mois en français
     */
    public static String getMoisEnFrancais(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH));
    }

    /**
     * Obtient le numéro de la semaine
     */
    public static int getNumeroSemaine(LocalDate date) {
        return date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
    }

    /**
     * Vérifie si une date est dans le passé
     */
    public static boolean estDansLePasse(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Vérifie si une date est dans le futur
     */
    public static boolean estDansLeFutur(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Calcule la durée entre deux heures en minutes
     */
    public static int calculerDureeEnMinutes(LocalTime debut, LocalTime fin) {
        return (int) java.time.Duration.between(debut, fin).toMinutes();
    }

    /**
     * Calcule l'heure de fin à partir d'une heure de début et d'une durée
     */
    public static LocalTime calculerHeureFin(LocalTime debut, int dureeMinutes) {
        return debut.plusMinutes(dureeMinutes);
    }

    /**
     * Génère une plage de dates entre deux dates - ✅ VERSION JAVA 11 COMPATIBLE
     */
    public static List<LocalDate> getPlageDates(LocalDate debut, LocalDate fin) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate courant = debut;
        while (!courant.isAfter(fin)) {
            dates.add(courant);
            courant = courant.plusDays(1);
        }
        return dates;
    }

    /**
     * Vérifie si deux plages horaires se chevauchent
     */
    public static boolean chevauchement(LocalTime debut1, LocalTime fin1,
                                        LocalTime debut2, LocalTime fin2) {
        return !fin1.isBefore(debut2) && !fin2.isBefore(debut1);
    }
}