package com.univ.scheduler.db;

import com.univ.scheduler.model.Cours;
import com.univ.scheduler.model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des cours
 */
public class CoursDAO implements DAO<Cours> {

    private Connection connection;
    private UtilisateurDAO utilisateurDAO;

    public CoursDAO() {
        this.connection = DBConnection.getConnection();
        this.utilisateurDAO = new UtilisateurDAO();
    }

    @Override
    public Cours getById(int id) {
        String sql = "SELECT c.*, u.nom as enseignant_nom, u.email as enseignant_email " +
                "FROM Cours c " +
                "LEFT JOIN Utilisateur u ON c.id_enseignant = u.id " +
                "WHERE c.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractCoursWithRelations(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getById cours: " + id);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Cours> getAll() {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT c.*, u.nom as enseignant_nom, u.email as enseignant_email " +
                "FROM Cours c " +
                "LEFT JOIN Utilisateur u ON c.id_enseignant = u.id " +
                "ORDER BY c.nom_matiere";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                coursList.add(extractCoursWithRelations(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll cours");
            e.printStackTrace();
        }
        return coursList;
    }

    @Override
    public boolean insert(Cours cours) {
        String sql = "INSERT INTO Cours (nom_matiere, id_enseignant, classe, groupe) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cours.getNomMatiere());
            stmt.setInt(2, cours.getIdEnseignant());
            stmt.setString(3, cours.getClasse());
            stmt.setString(4, cours.getGroupe());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    cours.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert cours: " + cours.getNomMatiere());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Cours cours) {
        String sql = "UPDATE Cours SET nom_matiere = ?, id_enseignant = ?, classe = ?, groupe = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cours.getNomMatiere());
            stmt.setInt(2, cours.getIdEnseignant());
            stmt.setString(3, cours.getClasse());
            stmt.setString(4, cours.getGroupe());
            stmt.setInt(5, cours.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update cours: " + cours.getId());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Cours WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete cours: " + id);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère les cours par enseignant
     */
    public List<Cours> getByEnseignant(int enseignantId) {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT c.*, u.nom as enseignant_nom, u.email as enseignant_email " +
                "FROM Cours c " +
                "LEFT JOIN Utilisateur u ON c.id_enseignant = u.id " +
                "WHERE c.id_enseignant = ? " +
                "ORDER BY c.nom_matiere";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, enseignantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                coursList.add(extractCoursWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coursList;
    }

    /**
     * Récupère les cours par classe
     */
    public List<Cours> getByClasse(String classe) {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT c.*, u.nom as enseignant_nom, u.email as enseignant_email " +
                "FROM Cours c " +
                "LEFT JOIN Utilisateur u ON c.id_enseignant = u.id " +
                "WHERE c.classe = ? " +
                "ORDER BY c.nom_matiere";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, classe);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                coursList.add(extractCoursWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coursList;
    }

    /**
     * Récupère les cours par classe et groupe
     */
    public List<Cours> getByClasseEtGroupe(String classe, String groupe) {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT c.*, u.nom as enseignant_nom, u.email as enseignant_email " +
                "FROM Cours c " +
                "LEFT JOIN Utilisateur u ON c.id_enseignant = u.id " +
                "WHERE c.classe = ? AND (c.groupe = ? OR c.groupe IS NULL) " +
                "ORDER BY c.nom_matiere";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, classe);
            stmt.setString(2, groupe);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                coursList.add(extractCoursWithRelations(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coursList;
    }

    private Cours extractCoursWithRelations(ResultSet rs) throws SQLException {
        Cours cours = new Cours();
        cours.setId(rs.getInt("id"));
        cours.setNomMatiere(rs.getString("nom_matiere"));
        cours.setIdEnseignant(rs.getInt("id_enseignant"));
        cours.setClasse(rs.getString("classe"));
        cours.setGroupe(rs.getString("groupe"));

        // Récupérer l'enseignant
        if (rs.getObject("id_enseignant") != null) {
            Utilisateur enseignant = new Utilisateur();
            enseignant.setId(rs.getInt("id_enseignant"));
            enseignant.setNom(rs.getString("enseignant_nom"));
            enseignant.setEmail(rs.getString("enseignant_email"));
            enseignant.setRole("Enseignant");
            cours.setEnseignant(enseignant);
        }

        return cours;
    }
}