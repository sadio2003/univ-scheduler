package com.univ.scheduler.db;

import com.univ.scheduler.model.Cours;
import com.univ.scheduler.model.Salle;
import com.univ.scheduler.model.Seance;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des séances (emploi du temps)
 */
public class SeanceDAO implements DAO<Seance> {

    private Connection connection;
    private CoursDAO coursDAO;
    private SalleDAO salleDAO;

    public SeanceDAO() {
        this.connection = DBConnection.getConnection();
        this.coursDAO = new CoursDAO();
        this.salleDAO = new SalleDAO();
    }

    @Override
    public Seance getById(int id) {
        String sql = "SELECT * FROM Seance WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractSeanceWithRelations(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getById seance: " + id);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Seance> getAll() {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM Seance ORDER BY jour_semaine, heure_debut";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                seances.add(extractSeanceWithRelations(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll seances");
            e.printStackTrace();
        }
        return seances;
    }

    @Override
    public boolean insert(Seance seance) {
        String sql = "INSERT INTO Seance (id_cours, id_salle, jour_semaine, heure_debut, duree) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, seance.getIdCours());
            stmt.setInt(2, seance.getIdSalle());
            stmt.setString(3, seance.getJourSemaine());
            stmt.setTime(4, Time.valueOf(seance.getHeureDebut()));
            stmt.setInt(5, seance.getDuree());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    seance.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert seance");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Seance seance) {
        String sql = "UPDATE Seance SET id_cours = ?, id_salle = ?, jour_semaine = ?, " +
                "heure_debut = ?, duree = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, seance.getIdCours());
            stmt.setInt(2, seance.getIdSalle());
            stmt.setString(3, seance.getJourSemaine());
            stmt.setTime(4, Time.valueOf(seance.getHeureDebut()));
            stmt.setInt(5, seance.getDuree());
            stmt.setInt(6, seance.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update seance: " + seance.getId());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Seance WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete seance: " + id);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère les séances par salle
     */
    public List<Seance> getBySalle(int salleId) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM Seance WHERE id_salle = ? ORDER BY jour_semaine, heure_debut";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(extractSeanceWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    /**
     * Récupère les séances par cours
     */
    public List<Seance> getByCours(int coursId) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM Seance WHERE id_cours = ? ORDER BY jour_semaine, heure_debut";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, coursId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(extractSeanceWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    /**
     * Récupère les séances par jour
     */
    public List<Seance> getByJour(String jour) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM Seance WHERE jour_semaine = ? ORDER BY heure_debut";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jour.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(extractSeanceWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    /**
     * Récupère les séances par enseignant
     */
    public List<Seance> getByEnseignant(int enseignantId) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT s.* FROM Seance s " +
                "JOIN Cours c ON s.id_cours = c.id " +
                "WHERE c.id_enseignant = ? " +
                "ORDER BY s.jour_semaine, s.heure_debut";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, enseignantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(extractSeanceWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    /**
     * Récupère les séances par classe
     */
    public List<Seance> getByClasse(String classe) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT s.* FROM Seance s " +
                "JOIN Cours c ON s.id_cours = c.id " +
                "WHERE c.classe = ? " +
                "ORDER BY s.jour_semaine, s.heure_debut";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, classe);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(extractSeanceWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    /**
     * Vérifie si une salle est disponible à un créneau donné
     */
    public boolean isSalleDisponible(int salleId, String jour, LocalTime heureDebut, int duree) {
        LocalTime heureFin = heureDebut.plusMinutes(duree);

        String sql = "SELECT COUNT(*) FROM Seance " +
                "WHERE id_salle = ? AND jour_semaine = ? " +
                "AND ((heure_debut <= ? AND ADDTIME(heure_debut, SEC_TO_TIME(duree*60)) > ?) " +
                "OR (heure_debut < ADDTIME(?, SEC_TO_TIME(?*60)) AND " +
                "ADDTIME(heure_debut, SEC_TO_TIME(duree*60)) > ?))";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            stmt.setString(2, jour);
            stmt.setTime(3, Time.valueOf(heureFin));
            stmt.setTime(4, Time.valueOf(heureDebut));
            stmt.setTime(5, Time.valueOf(heureDebut));
            stmt.setInt(6, duree);
            stmt.setTime(7, Time.valueOf(heureDebut));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Seance extractSeanceWithRelations(ResultSet rs) throws SQLException {
        Seance seance = new Seance();
        seance.setId(rs.getInt("id"));
        seance.setIdCours(rs.getInt("id_cours"));
        seance.setIdSalle(rs.getInt("id_salle"));
        seance.setJourSemaine(rs.getString("jour_semaine"));
        seance.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
        seance.setDuree(rs.getInt("duree"));

        // Charger les objets associés
        Cours cours = coursDAO.getById(seance.getIdCours());
        seance.setCours(cours);

        Salle salle = salleDAO.getById(seance.getIdSalle());
        seance.setSalle(salle);

        return seance;
    }
}