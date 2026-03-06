-- =====================================================
-- SCRIPT DE CRÉATION DE LA BASE DE DONNÉES
-- UNIV-SCHEDULER - Application de gestion des emplois du temps
-- Université du Sénégal (UIDT)
-- =====================================================

-- Supprimer la base si elle existe (pour une réinstallation propre)
DROP DATABASE IF EXISTS univ_scheduler;

-- Créer la base de données
CREATE DATABASE univ_scheduler
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Utiliser la base
USE univ_scheduler;

-- =====================================================
-- CRÉATION DES TABLES
-- =====================================================

-- -----------------------------------------------------
-- Table Utilisateur
-- -----------------------------------------------------
CREATE TABLE Utilisateur (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             nom VARCHAR(100) NOT NULL,
                             email VARCHAR(100) UNIQUE NOT NULL,
                             mot_de_passe VARCHAR(255) NOT NULL,
                             role ENUM('Admin', 'Gestionnaire', 'Enseignant', 'Etudiant') NOT NULL DEFAULT 'Etudiant',
                             date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             derniere_connexion TIMESTAMP NULL,
                             actif BOOLEAN DEFAULT TRUE,
                             INDEX idx_email (email),
                             INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table Batiment
-- -----------------------------------------------------
CREATE TABLE Batiment (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          nom VARCHAR(100) NOT NULL,
                          localisation VARCHAR(255),
                          description TEXT,
                          date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          INDEX idx_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table Equipement
-- -----------------------------------------------------
CREATE TABLE Equipement (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            nom VARCHAR(100) UNIQUE NOT NULL,
                            description TEXT,
                            icone VARCHAR(50),
                            INDEX idx_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table Salle
-- -----------------------------------------------------
CREATE TABLE Salle (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       numero VARCHAR(20) UNIQUE NOT NULL,
                       capacite INT NOT NULL CHECK (capacite > 0),
                       type ENUM('TD', 'TP', 'Amphi', 'Réunion', 'Autre') NOT NULL DEFAULT 'TD',
                       id_batiment INT NOT NULL,
                       description TEXT,
                       etage INT DEFAULT 0,
                       accessible_handicap BOOLEAN DEFAULT TRUE,
                       prise_reseau BOOLEAN DEFAULT TRUE,
                       wifi BOOLEAN DEFAULT TRUE,
                       date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (id_batiment) REFERENCES Batiment(id) ON DELETE RESTRICT,
                       INDEX idx_numero (numero),
                       INDEX idx_type (type),
                       INDEX idx_batiment (id_batiment),
                       INDEX idx_capacite (capacite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table de liaison Salle_Equipement
-- -----------------------------------------------------
CREATE TABLE Salle_Equipement (
                                  id_salle INT NOT NULL,
                                  id_equipement INT NOT NULL,
                                  quantite INT DEFAULT 1 CHECK (quantite > 0),
                                  date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (id_salle, id_equipement),
                                  FOREIGN KEY (id_salle) REFERENCES Salle(id) ON DELETE CASCADE,
                                  FOREIGN KEY (id_equipement) REFERENCES Equipement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table Cours
-- -----------------------------------------------------
CREATE TABLE Cours (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       nom_matiere VARCHAR(100) NOT NULL,
                       code_matiere VARCHAR(20),
                       id_enseignant INT NOT NULL,
                       classe VARCHAR(50) NOT NULL,
                       groupe VARCHAR(20),
                       credit INT DEFAULT 0,
                       description TEXT,
                       semestre VARCHAR(20),
                       annee_universitaire VARCHAR(20),
                       date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (id_enseignant) REFERENCES Utilisateur(id) ON DELETE RESTRICT,
                       INDEX idx_matiere (nom_matiere),
                       INDEX idx_enseignant (id_enseignant),
                       INDEX idx_classe (classe),
                       UNIQUE KEY unique_cours (nom_matiere, classe, groupe, semestre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table Seance
-- -----------------------------------------------------
CREATE TABLE Seance (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        id_cours INT NOT NULL,
                        id_salle INT NOT NULL,
                        jour_semaine ENUM('LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI', 'DIMANCHE') NOT NULL,
                        heure_debut TIME NOT NULL,
                        duree INT NOT NULL CHECK (duree > 0 AND duree <= 240),
                        type_seance ENUM('Cours', 'TD', 'TP', 'Examen', 'Soutenance', 'Réunion') DEFAULT 'Cours',
                        periode_debut DATE,
                        periode_fin DATE,
                        recurrence VARCHAR(20),
                        description TEXT,
                        date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (id_cours) REFERENCES Cours(id) ON DELETE CASCADE,
                        FOREIGN KEY (id_salle) REFERENCES Salle(id) ON DELETE CASCADE,
                        INDEX idx_jour (jour_semaine),
                        INDEX idx_salle (id_salle),
                        INDEX idx_cours (id_cours),
                        INDEX idx_horaire (heure_debut),
                        CONSTRAINT check_horaire CHECK (heure_debut BETWEEN '07:00:00' AND '22:00:00')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table Reservation (pour les réservations ponctuelles)
-- -----------------------------------------------------
CREATE TABLE Reservation (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             id_utilisateur INT NOT NULL,
                             id_salle INT NOT NULL,
                             titre VARCHAR(100) NOT NULL,
                             description TEXT,
                             date_reservation DATE NOT NULL,
                             heure_debut TIME NOT NULL,
                             heure_fin TIME NOT NULL,
                             statut ENUM('En attente', 'Confirmée', 'Annulée', 'Terminée') DEFAULT 'En attente',
                             motif VARCHAR(255),
                             date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE,
                             FOREIGN KEY (id_salle) REFERENCES Salle(id) ON DELETE CASCADE,
                             INDEX idx_date (date_reservation),
                             INDEX idx_utilisateur (id_utilisateur),
                             INDEX idx_statut (statut),
                             CONSTRAINT check_horaire_reservation CHECK (heure_fin > heure_debut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table Notification
-- -----------------------------------------------------
CREATE TABLE Notification (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              id_utilisateur INT NOT NULL,
                              titre VARCHAR(100) NOT NULL,
                              message TEXT NOT NULL,
                              type ENUM('Info', 'Succès', 'Avertissement', 'Erreur') DEFAULT 'Info',
                              lu BOOLEAN DEFAULT FALSE,
                              date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id) ON DELETE CASCADE,
                              INDEX idx_utilisateur (id_utilisateur),
                              INDEX idx_lu (lu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- INSERTION DES DONNÉES DE TEST
-- =====================================================

-- -----------------------------------------------------
-- Utilisateurs
-- -----------------------------------------------------
INSERT INTO Utilisateur (nom, email, mot_de_passe, role) VALUES
-- Admins
('Admin Système', 'admin@uidt.sn', 'admin123', 'Admin'),
('Admin UIDT', 'admin2@uidt.sn', 'admin123', 'Admin'),

-- Gestionnaires
('Mamadou Diop', 'gestionnaire@uidt.sn', 'gestion123', 'Gestionnaire'),
('Fatou Ndiaye', 'fatou.ndiaye@uidt.sn', 'gestion123', 'Gestionnaire'),

-- Enseignants
('Dr. Amadou Fall', 'amadou.fall@uidt.sn', 'ens123', 'Enseignant'),
('Pr. Aïssatou Seck', 'aissatou.seck@uidt.sn', 'ens123', 'Enseignant'),
('Dr. Ibrahima Cissé', 'ibrahima.cisse@uidt.sn', 'ens123', 'Enseignant'),
('Mme. Khady Dieng', 'khady.dieng@uidt.sn', 'ens123', 'Enseignant'),
('Dr. Ousmane Sow', 'ousmane.sow@uidt.sn', 'ens123', 'Enseignant'),

-- Étudiants
('Alioune Ndiaye', 'alioune.ndiaye@etu.uidt.sn', 'etud123', 'Etudiant'),
('Mariama Diallo', 'mariama.diallo@etu.uidt.sn', 'etud123', 'Etudiant'),
('Cheikh Diop', 'cheikh.diop@etu.uidt.sn', 'etud123', 'Etudiant'),
('Aminata Sarr', 'aminata.sarr@etu.uidt.sn', 'etud123', 'Etudiant'),
('Moussa Ba', 'moussa.ba@etu.uidt.sn', 'etud123', 'Etudiant'),
('Astou Ndiaye', 'astou.ndiaye@etu.uidt.sn', 'etud123', 'Etudiant');

-- -----------------------------------------------------
-- Bâtiments
-- -----------------------------------------------------
INSERT INTO Batiment (nom, localisation, description) VALUES
                                                          ('Bâtiment A - Pédagogique', 'Campus principal', 'Bâtiment principal des cours de Licence'),
                                                          ('Bâtiment B - Sciences', 'Campus principal', 'Laboratoires et salles de TP'),
                                                          ('Bâtiment C - Amphis', 'Campus principal', 'Amphithéâtres de 200 places'),
                                                          ('Bibliothèque Universitaire', 'Centre campus', 'Salles d\'étude et de recherche'),
('Département Informatique', 'Campus Est', 'Département LI et GI'),
('Département Mathématiques', 'Campus Est', 'Département de mathématiques'),
('Bâtiment Administratif', 'Entrée campus', 'Administration et scolarité'),
('Restaurant Universitaire', 'Campus Ouest', 'Espace de restauration');

-- -----------------------------------------------------
-- Équipements
-- -----------------------------------------------------
INSERT INTO Equipement (nom, description, icone) VALUES
('Vidéoprojecteur', 'Projecteur HD avec HDMI', 'projector.png'),
('Tableau interactif', 'Tableau numérique tactile', 'smartboard.png'),
('Climatisation', 'Climatisation réversible', 'ac.png'),
('Ordinateur fixe', 'PC avec écran 24"', 'pc.png'),
('Connexion Wi-Fi', 'Accès internet sans fil', 'wifi.png'),
                                                           ('Prise réseau', 'Prise Ethernet murale', 'ethernet.png'),
                                                           ('Tableau blanc', 'Tableau blanc effaçable', 'whiteboard.png'),
                                                           ('Microphone', 'Système de sonorisation', 'mic.png'),
                                                           ('Caméra', 'Système de visioconférence', 'camera.png'),
                                                           ('Chaise ergonomique', 'Chaise confortable', 'chair.png');

-- -----------------------------------------------------
-- Salles
-- -----------------------------------------------------
INSERT INTO Salle (numero, capacite, type, id_batiment, etage, accessible_handicap, description) VALUES
-- Bâtiment A
('A101', 30, 'TD', 1, 1, TRUE, 'Salle de TD standard'),
('A102', 30, 'TD', 1, 1, TRUE, 'Salle de TD standard'),
('A103', 40, 'TD', 1, 1, TRUE, 'Grande salle de TD'),
('A201', 25, 'TP', 1, 2, TRUE, 'Salle de TP informatique'),
('A202', 25, 'TP', 1, 2, FALSE, 'Salle de TP électronique'),

-- Bâtiment B
('B01', 50, 'TP', 2, 0, TRUE, 'Labo de chimie'),
('B02', 50, 'TP', 2, 0, TRUE, 'Labo de physique'),
('B101', 20, 'TP', 2, 1, TRUE, 'Salle de TP info 1'),
('B102', 20, 'TP', 2, 1, TRUE, 'Salle de TP info 2'),
('B201', 30, 'TD', 2, 2, TRUE, 'Salle de TD sciences'),

-- Bâtiment C (Amphis)
('C001', 200, 'Amphi', 3, 0, TRUE, 'Grand amphithéâtre A'),
('C002', 150, 'Amphi', 3, 0, TRUE, 'Amphithéâtre B'),
('C003', 100, 'Amphi', 3, 1, TRUE, 'Petit amphithéâtre'),

-- Bibliothèque
('BIB01', 50, 'Réunion', 4, 1, TRUE, 'Salle de travail'),
('BIB02', 30, 'Réunion', 4, 1, TRUE, 'Salle de réunion'),
('BIB03', 100, 'Réunion', 4, 2, TRUE, 'Espace étude'),

-- Département Info
('D101', 25, 'TP', 5, 1, TRUE, 'Labo Java'),
('D102', 25, 'TP', 5, 1, TRUE, 'Labo Python'),
('D103', 40, 'TD', 5, 1, TRUE, 'Salle de cours L2'),
('D201', 20, 'TP', 5, 2, TRUE, 'Labo Réseaux'),
('D202', 20, 'TP', 5, 2, TRUE, 'Labo Base de données');

-- -----------------------------------------------------
-- Équipements des salles
-- -----------------------------------------------------
-- Salle A101
INSERT INTO Salle_Equipement (id_salle, id_equipement, quantite) VALUES
                                                                     (1, 1, 1), (1, 2, 1), (1, 3, 1), (1, 5, 1), (1, 7, 1);

-- Salle A102
INSERT INTO Salle_Equipement (id_salle, id_equipement, quantite) VALUES
                                                                     (2, 1, 1), (2, 3, 1), (2, 5, 1), (2, 7, 1);

-- Salle A103
INSERT INTO Salle_Equipement (id_salle, id_equipement, quantite) VALUES
                                                                     (3, 1, 2), (3, 2, 1), (3, 3, 2), (3, 5, 1), (3, 7, 2);

-- Salles TP informatique (A201, A202, B101, B102, D101, D102)
INSERT INTO Salle_Equipement (id_salle, id_equipement, quantite) VALUES
                                                                     (4, 4, 25), (4, 5, 1), (4, 6, 25),
                                                                     (5, 4, 25), (5, 5, 1), (5, 6, 25),
                                                                     (10, 4, 20), (10, 5, 1), (10, 6, 20),
                                                                     (11, 4, 20), (11, 5, 1), (11, 6, 20),
                                                                     (17, 4, 25), (17, 5, 1), (17, 6, 25),
                                                                     (18, 4, 25), (18, 5, 1), (18, 6, 25);

-- Amphis (C001, C002, C003)
INSERT INTO Salle_Equipement (id_salle, id_equipement, quantite) VALUES
                                                                     (12, 1, 2), (12, 3, 4), (12, 5, 1), (12, 8, 2), (12, 9, 1),
                                                                     (13, 1, 2), (13, 3, 3), (13, 5, 1), (13, 8, 1), (13, 9, 1),
                                                                     (14, 1, 1), (14, 3, 2), (14, 5, 1), (14, 8, 1);

-- -----------------------------------------------------
-- Cours
-- -----------------------------------------------------
INSERT INTO Cours (nom_matiere, code_matiere, id_enseignant, classe, groupe, credit, semestre, annee_universitaire) VALUES
-- LI2 (Licence Informatique 2)
('Programmation Java', 'LI201', 5, 'LI2', 'G1', 4, 'S1', '2025-2026'),
('Programmation Java', 'LI201', 5, 'LI2', 'G2', 4, 'S1', '2025-2026'),
('Base de données', 'LI202', 6, 'LI2', 'G1', 4, 'S1', '2025-2026'),
('Base de données', 'LI202', 6, 'LI2', 'G2', 4, 'S1', '2025-2026'),
('Réseaux', 'LI203', 7, 'LI2', 'G1', 3, 'S1', '2025-2026'),
('Réseaux', 'LI203', 7, 'LI2', 'G2', 3, 'S1', '2025-2026'),
('Algorithmique', 'LI204', 8, 'LI2', 'G1', 4, 'S1', '2025-2026'),
('Algorithmique', 'LI204', 8, 'LI2', 'G2', 4, 'S1', '2025-2026'),
('Anglais technique', 'LI205', 9, 'LI2', 'G1', 2, 'S1', '2025-2026'),
('Anglais technique', 'LI205', 9, 'LI2', 'G2', 2, 'S1', '2025-2026'),

-- GI2 (Génie Informatique 2)
('Systèmes d\'exploitation', 'GI201', 5, 'GI2', 'G1', 4, 'S1', '2025-2026'),
('Systèmes d\'exploitation', 'GI201', 5, 'GI2', 'G2', 4, 'S1', '2025-2026'),
('Génie logiciel', 'GI202', 6, 'GI2', 'G1', 4, 'S1', '2025-2026'),
('Génie logiciel', 'GI202', 6, 'GI2', 'G2', 4, 'S1', '2025-2026'),
('Architecture', 'GI203', 7, 'GI2', 'G1', 3, 'S1', '2025-2026'),
('Architecture', 'GI203', 7, 'GI2', 'G2', 3, 'S1', '2025-2026');

-- -----------------------------------------------------
-- Séances (Emploi du temps)
-- -----------------------------------------------------
INSERT INTO Seance (id_cours, id_salle, jour_semaine, heure_debut, duree, type_seance, periode_debut, periode_fin) VALUES
-- LI2 G1 - Lundi
(1, 18, 'LUNDI', '08:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),
(3, 1, 'LUNDI', '10:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),
(5, 10, 'LUNDI', '14:00:00', 120, 'TP', '2026-03-01', '2026-06-15'),

-- LI2 G2 - Lundi
(2, 19, 'LUNDI', '08:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),
(4, 2, 'LUNDI', '10:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),
(6, 11, 'LUNDI', '14:00:00', 120, 'TP', '2026-03-01', '2026-06-15'),

-- LI2 G1 - Mardi
(7, 3, 'MARDI', '08:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),
(9, 1, 'MARDI', '10:00:00', 60, 'Cours', '2026-03-01', '2026-06-15'),

-- LI2 G2 - Mardi
(8, 3, 'MARDI', '14:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),
(10, 2, 'MARDI', '16:00:00', 60, 'Cours', '2026-03-01', '2026-06-15'),

-- Mercredi - TP
(5, 17, 'MERCREDI', '08:00:00', 120, 'TP', '2026-03-01', '2026-06-15'),
(6, 18, 'MERCREDI', '10:00:00', 120, 'TP', '2026-03-01', '2026-06-15'),

-- Jeudi
(1, 19, 'JEUDI', '08:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),
(3, 12, 'JEUDI', '10:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),

-- Vendredi
(7, 13, 'VENDREDI', '08:00:00', 90, 'Cours', '2026-03-01', '2026-06-15'),
(9, 14, 'VENDREDI', '10:00:00', 60, 'Cours', '2026-03-01', '2026-06-15');

-- -----------------------------------------------------
-- Notifications de test
-- -----------------------------------------------------
INSERT INTO Notification (id_utilisateur, titre, message, type, lu) VALUES
                                                                        (5, 'Changement de salle', 'Le cours de Java est déplacé en salle A103', 'Info', FALSE),
                                                                        (5, 'Réunion pédagogique', 'Réunion des enseignants le 15/03 à 14h', 'Avertissement', FALSE),
                                                                        (6, 'Emploi du temps modifié', 'Votre emploi du temps a été mis à jour', 'Info', TRUE),
                                                                        (1, 'Nouvel utilisateur', '5 nouveaux étudiants ont été inscrits', 'Succès', FALSE),
                                                                        (2, 'Maintenance', 'Le bâtiment A sera fermé samedi', 'Erreur', TRUE);

-- =====================================================
-- CRÉATION DES VUES
-- =====================================================

-- Vue des salles avec leurs équipements
CREATE VIEW vue_salles_complete AS
SELECT
    s.id,
    s.numero,
    s.capacite,
    s.type,
    b.nom as batiment,
    b.localisation,
    s.etage,
    s.accessible_handicap,
    GROUP_CONCAT(DISTINCT e.nom SEPARATOR ', ') as equipements,
    COUNT(DISTINCT se.id_cours) as nb_cours
FROM Salle s
         LEFT JOIN Batiment b ON s.id_batiment = b.id
         LEFT JOIN Salle_Equipement se_eq ON s.id = se_eq.id_salle
         LEFT JOIN Equipement e ON se_eq.id_equipement = e.id
         LEFT JOIN Seance se ON s.id = se.id_salle
GROUP BY s.id;

-- Vue des emplois du temps par classe
CREATE VIEW vue_emploi_temps_classe AS
SELECT
    c.classe,
    c.groupe,
    c.nom_matiere,
    u.nom as enseignant,
    s.numero as salle,
    se.jour_semaine,
    se.heure_debut,
    se.duree,
    ADDTIME(se.heure_debut, SEC_TO_TIME(se.duree*60)) as heure_fin
FROM Seance se
         JOIN Cours c ON se.id_cours = c.id
         JOIN Utilisateur u ON c.id_enseignant = u.id
         JOIN Salle s ON se.id_salle = s.id
ORDER BY c.classe, c.groupe, FIELD(se.jour_semaine, 'LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI'), se.heure_debut;

-- Vue des taux d'occupation des salles
CREATE VIEW vue_occupation_salles AS
SELECT
    s.id,
    s.numero,
    s.capacite,
    COUNT(se.id) as nb_seances,
    (COUNT(se.id) * 100.0 / 30) as taux_occupation
FROM Salle s
         LEFT JOIN Seance se ON s.id = se.id_salle
GROUP BY s.id;

-- =====================================================
-- CRÉATION DES INDEX POUR OPTIMISATION
-- =====================================================

CREATE INDEX idx_seance_horaire ON Seance(jour_semaine, heure_debut);
CREATE INDEX idx_seance_salle_date ON Seance(id_salle, jour_semaine);
CREATE INDEX idx_reservation_date ON Reservation(date_reservation, heure_debut);
CREATE INDEX idx_notification_user_lu ON Notification(id_utilisateur, lu);

-- =====================================================
-- CRÉATION D'UN UTILISATEUR POUR L'APPLICATION
-- =====================================================

-- Créer un utilisateur dédié pour l'application (optionnel)
-- CREATE USER 'app_univ'@'localhost' IDENTIFIED BY 'MotDePasse123!';
-- GRANT ALL PRIVILEGES ON univ_scheduler.* TO 'app_univ'@'localhost';
-- FLUSH PRIVILEGES;

-- =====================================================
-- FIN DU SCRIPT
-- =====================================================

-- Message de confirmation
SELECT '✅ Base de données univ_scheduler créée avec succès !' as Message;