package com.univ.scheduler.model;

import com.univ.scheduler.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour les classes du modèle
 * Vérifie le bon fonctionnement des entités métier
 */
public class ModeleTest {

    private Utilisateur utilisateur;
    private Batiment batiment;
    private Equipement equipement;
    private Salle salle;
    private Cours cours;
    private Seance seance;

    @BeforeEach
    void setUp() {
        // Initialisation des objets de test
        utilisateur = new Utilisateur(1, "Dr. Diop", "diop@uidt.sn", "password123", "Enseignant");
        batiment = new Batiment(1, "Bâtiment pédagogique", "Campus universitaire");
        equipement = new Equipement(1, "Vidéoprojecteur");
        salle = new Salle(1, "TD101", 30, "TD", 1);
        cours = new Cours(1, "Programmation Java", 1, "LI2", "G1");
        seance = new Seance(1, 1, 1, "LUNDI", LocalTime.of(8, 0), 90);
    }

    // ==================== TESTS UTILISATEUR ====================

    @Test
    void testUtilisateurConstructeur() {
        assertNotNull(utilisateur);
        assertEquals(1, utilisateur.getId());
        assertEquals("Dr. Diop", utilisateur.getNom());
        assertEquals("diop@uidt.sn", utilisateur.getEmail());
        assertEquals("password123", utilisateur.getMotDePasse());
        assertEquals("Enseignant", utilisateur.getRole());
    }

    @Test
    void testUtilisateurSetters() {
        Utilisateur u = new Utilisateur();
        u.setId(2);
        u.setNom("Pr. Ndiaye");
        u.setEmail("ndiaye@uidt.sn");
        u.setMotDePasse("newpass");
        u.setRole("Admin");

        assertEquals(2, u.getId());
        assertEquals("Pr. Ndiaye", u.getNom());
        assertEquals("ndiaye@uidt.sn", u.getEmail());
        assertEquals("newpass", u.getMotDePasse());
        assertEquals("Admin", u.getRole());
    }

    @Test
    void testUtilisateurRoles() {
        Utilisateur admin = new Utilisateur(2, "Admin", "admin@uidt.sn", "Admin");
        Utilisateur gestionnaire = new Utilisateur(3, "Gestionnaire", "gestion@uidt.sn", "Gestionnaire");
        Utilisateur enseignant = new Utilisateur(4, "Enseignant", "ens@uidt.sn", "Enseignant");
        Utilisateur etudiant = new Utilisateur(5, "Etudiant", "etud@uidt.sn", "Etudiant");

        assertTrue(admin.isAdmin());
        assertFalse(admin.isEnseignant());

        assertTrue(gestionnaire.isGestionnaire());
        assertTrue(enseignant.isEnseignant());
        assertTrue(etudiant.isEtudiant());
    }

    @Test
    void testUtilisateurToString() {
        String expected = "Dr. Diop (Enseignant)";
        assertEquals(expected, utilisateur.toString());
    }

    @Test
    void testUtilisateurEquals() {
        Utilisateur u1 = new Utilisateur(1, "Dr. Diop", "diop@uidt.sn", "Enseignant");
        Utilisateur u2 = new Utilisateur(1, "Dr. Diop", "diop@uidt.sn", "Enseignant");
        Utilisateur u3 = new Utilisateur(2, "Autre", "autre@uidt.sn", "Etudiant");

        assertEquals(u1, u2);
        assertNotEquals(u1, u3);
        assertEquals(u1.hashCode(), u2.hashCode());
        assertNotEquals(u1.hashCode(), u3.hashCode());
    }

    // ==================== TESTS BATIMENT ====================

    @Test
    void testBatimentConstructeur() {
        assertNotNull(batiment);
        assertEquals(1, batiment.getId());
        assertEquals("Bâtiment pédagogique", batiment.getNom());
        assertEquals("Campus universitaire", batiment.getLocalisation());
    }

    @Test
    void testBatimentSetters() {
        Batiment b = new Batiment();
        b.setId(2);
        b.setNom("Bibliothèque");
        b.setLocalisation("Centre ville");

        assertEquals(2, b.getId());
        assertEquals("Bibliothèque", b.getNom());
        assertEquals("Centre ville", b.getLocalisation());
    }

    @Test
    void testBatimentToString() {
        assertEquals("Bâtiment pédagogique", batiment.toString());
    }

    // ==================== TESTS EQUIPEMENT ====================

    @Test
    void testEquipementConstructeur() {
        assertNotNull(equipement);
        assertEquals(1, equipement.getId());
        assertEquals("Vidéoprojecteur", equipement.getNom());
    }

    @Test
    void testEquipementSetters() {
        Equipement e = new Equipement();
        e.setId(2);
        e.setNom("Tableau interactif");

        assertEquals(2, e.getId());
        assertEquals("Tableau interactif", e.getNom());
    }

    @Test
    void testEquipementToString() {
        assertEquals("Vidéoprojecteur", equipement.toString());
    }

    // ==================== TESTS SALLE ====================

    @Test
    void testSalleConstructeur() {
        assertNotNull(salle);
        assertEquals(1, salle.getId());
        assertEquals("TD101", salle.getNumero());
        assertEquals(30, salle.getCapacite());
        assertEquals("TD", salle.getType());
        assertEquals(1, salle.getIdBatiment());
    }

    @Test
    void testSalleSetters() {
        Salle s = new Salle();
        s.setId(2);
        s.setNumero("TP202");
        s.setCapacite(20);
        s.setType("TP");
        s.setIdBatiment(2);

        Batiment b = new Batiment(2, "Sciences", "Campus");
        s.setBatiment(b);

        assertEquals(2, s.getId());
        assertEquals("TP202", s.getNumero());
        assertEquals(20, s.getCapacite());
        assertEquals("TP", s.getType());
        assertEquals(2, s.getIdBatiment());
        assertEquals(b, s.getBatiment());
    }

    @Test
    void testSalleEquipements() {
        assertTrue(salle.getEquipements().isEmpty());

        Equipement e1 = new Equipement(1, "Vidéoprojecteur");
        Equipement e2 = new Equipement(2, "Climatisation");

        salle.addEquipement(e1);
        salle.addEquipement(e2);

        assertEquals(2, salle.getEquipements().size());
        assertTrue(salle.hasEquipement("Vidéoprojecteur"));
        assertTrue(salle.hasEquipement("Climatisation"));
        assertFalse(salle.hasEquipement("Tableau"));

        salle.removeEquipement(e1);
        assertEquals(1, salle.getEquipements().size());

        List<String> noms = salle.getEquipementsNoms();
        assertTrue(noms.contains("Climatisation"));

        String description = salle.getEquipementsDescription();
        assertTrue(description.contains("Climatisation"));
    }

    @Test
    void testSalleTypes() {
        Salle td = new Salle(1, "TD", 30, "TD", 1);
        Salle tp = new Salle(2, "TP", 20, "TP", 1);
        Salle amphi = new Salle(3, "Amphi", 200, "Amphi", 1);

        assertTrue(td.isTD());
        assertFalse(td.isTP());
        assertFalse(td.isAmphi());

        assertTrue(tp.isTP());
        assertTrue(amphi.isAmphi());
    }

    @Test
    void testSalleToString() {
        assertEquals("TD101 (30 places, TD)", salle.toString());
    }

    // ==================== TESTS COURS ====================

    @Test
    void testCoursConstructeur() {
        assertNotNull(cours);
        assertEquals(1, cours.getId());
        assertEquals("Programmation Java", cours.getNomMatiere());
        assertEquals(1, cours.getIdEnseignant());
        assertEquals("LI2", cours.getClasse());
        assertEquals("G1", cours.getGroupe());
    }

    @Test
    void testCoursSetters() {
        Cours c = new Cours();
        c.setId(2);
        c.setNomMatiere("Base de données");
        c.setIdEnseignant(2);
        c.setClasse("GI2");
        c.setGroupe("G2");

        Utilisateur ens = new Utilisateur(2, "M. Fall", "fall@uidt.sn", "Enseignant");
        c.setEnseignant(ens);

        assertEquals(2, c.getId());
        assertEquals("Base de données", c.getNomMatiere());
        assertEquals(2, c.getIdEnseignant());
        assertEquals("GI2", c.getClasse());
        assertEquals("G2", c.getGroupe());
        assertEquals(ens, c.getEnseignant());
    }

    @Test
    void testCoursClasseComplete() {
        assertEquals("LI2 G1", cours.getClasseComplete());

        Cours c2 = new Cours(2, "Maths", 1, "LI3", null);
        assertEquals("LI3", c2.getClasseComplete());
        assertFalse(c2.hasGroupe());
    }

    @Test
    void testCoursToString() {
        assertEquals("Programmation Java - LI2 G1", cours.toString());
    }

    // ==================== TESTS SEANCE ====================

    @Test
    void testSeanceConstructeur() {
        assertNotNull(seance);
        assertEquals(1, seance.getId());
        assertEquals(1, seance.getIdCours());
        assertEquals(1, seance.getIdSalle());
        assertEquals("LUNDI", seance.getJourSemaine());
        assertEquals(LocalTime.of(8, 0), seance.getHeureDebut());
        assertEquals(90, seance.getDuree());
    }

    @Test
    void testSeanceSetters() {
        Seance s = new Seance();
        s.setId(2);
        s.setIdCours(2);
        s.setIdSalle(2);
        s.setJourSemaine("MARDI");
        s.setHeureDebut(LocalTime.of(10, 0));
        s.setDuree(120);

        Cours c = new Cours(2, "Réseaux", 2, "GI2", "G1");
        s.setCours(c);

        Salle sal = new Salle(2, "TP202", 20, "TP", 1);
        s.setSalle(sal);

        assertEquals(2, s.getId());
        assertEquals(2, s.getIdCours());
        assertEquals(2, s.getIdSalle());
        assertEquals("MARDI", s.getJourSemaine());
        assertEquals(LocalTime.of(10, 0), s.getHeureDebut());
        assertEquals(120, s.getDuree());
        assertEquals(c, s.getCours());
        assertEquals(sal, s.getSalle());
    }

    @Test
    void testSeanceHeureFin() {
        LocalTime fin = seance.getHeureFin();
        assertEquals(LocalTime.of(9, 30), fin);

        Seance s2 = new Seance(2, 2, 2, "MARDI", LocalTime.of(14, 0), 120);
        assertEquals(LocalTime.of(16, 0), s2.getHeureFin());
    }

    @Test
    void testSeanceHoraireFormat() {
        String horaire = seance.getHoraireFormat();
        assertEquals("08:00 - 09:30", horaire);
    }

    @Test
    void testSeanceMatinApresMidi() {
        assertTrue(seance.isMatin());
        assertFalse(seance.isApresMidi());

        Seance s2 = new Seance(2, 2, 2, "MARDI", LocalTime.of(14, 0), 90);
        assertFalse(s2.isMatin());
        assertTrue(s2.isApresMidi());
    }

    @Test
    void testSeanceJourFrancais() {
        assertEquals("Lundi", seance.getJourFrancais());

        Seance s2 = new Seance();
        s2.setJourSemaine("MARDI");
        assertEquals("Mardi", s2.getJourFrancais());

        s2.setJourSemaine("MERCREDI");
        assertEquals("Mercredi", s2.getJourFrancais());

        s2.setJourSemaine("JEUDI");
        assertEquals("Jeudi", s2.getJourFrancais());

        s2.setJourSemaine("VENDREDI");
        assertEquals("Vendredi", s2.getJourFrancais());

        s2.setJourSemaine("SAMEDI");
        assertEquals("Samedi", s2.getJourFrancais());
    }

    @Test
    void testSeanceToString() {
        String expected = "Lundi 08:00 - 09:30 - ? (?)";
        assertEquals(expected, seance.toString());
    }

    // ==================== TESTS D'INTÉGRATION ====================

    @Test
    void testRelationsEntreObjets() {
        // Associer le bâtiment à la salle
        salle.setBatiment(batiment);
        assertEquals(batiment, salle.getBatiment());
        assertEquals(batiment.getId(), salle.getIdBatiment());

        // Associer l'enseignant au cours
        cours.setEnseignant(utilisateur);
        assertEquals(utilisateur, cours.getEnseignant());
        assertEquals(utilisateur.getId(), cours.getIdEnseignant());

        // Associer le cours et la salle à la séance
        seance.setCours(cours);
        seance.setSalle(salle);

        assertEquals(cours, seance.getCours());
        assertEquals(salle, seance.getSalle());
        assertEquals(cours.getId(), seance.getIdCours());
        assertEquals(salle.getId(), seance.getIdSalle());

        // Vérifier la cohérence
        assertEquals("Dr. Diop", seance.getCours().getEnseignant().getNom());
        assertEquals("Bâtiment pédagogique", seance.getSalle().getBatiment().getNom());
    }

    @Test
    void testListeEquipementsSalle() {
        List<Equipement> equipements = new ArrayList<>();
        equipements.add(new Equipement(1, "Vidéoprojecteur"));
        equipements.add(new Equipement(2, "Tableau interactif"));
        equipements.add(new Equipement(3, "Climatisation"));

        salle.setEquipements(equipements);

        assertEquals(3, salle.getEquipements().size());
        assertTrue(salle.hasEquipement("Vidéoprojecteur"));
        assertTrue(salle.hasEquipement("Climatisation"));

        String description = salle.getEquipementsDescription();
        assertTrue(description.contains("Vidéoprojecteur"));
        assertTrue(description.contains("Tableau interactif"));
    }
}