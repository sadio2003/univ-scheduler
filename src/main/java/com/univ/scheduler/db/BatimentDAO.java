package com.univ.scheduler.db;

import com.univ.scheduler.model.Batiment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des bâtiments
 */
public class BatimentDAO implements DAO<Batiment> {

    private Connection connection;

    public BatimentDAO() {
        this.connection = DBConnection.getConnection();
    }

    @Override
    public Batiment getById(int id) {
        String sql = "SELECT * FROM Batiment WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractBatimentFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getById batiment: " + id);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Batiment> getAll() {
        List<Batiment> batiments = new ArrayList<>();
        String sql = "SELECT * FROM Batiment ORDER BY nom";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                batiments.add(extractBatimentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll batiments");
            e.printStackTrace();
        }
        return batiments;
    }

    @Override
    public boolean insert(Batiment batiment) {
        String sql = "INSERT INTO Batiment (nom, localisation) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, batiment.getNom());
            stmt.setString(2, batiment.getLocalisation());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    batiment.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert batiment: " + batiment.getNom());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Batiment batiment) {
        String sql = "UPDATE Batiment SET nom = ?, localisation = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, batiment.getNom());
            stmt.setString(2, batiment.getLocalisation());
            stmt.setInt(3, batiment.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update batiment: " + batiment.getId());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Batiment WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete batiment: " + id);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère le nombre de salles dans un bâtiment
     */
    public int getNombreSalles(int batimentId) {
        String sql = "SELECT COUNT(*) FROM Salle WHERE id_batiment = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, batimentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Batiment extractBatimentFromResultSet(ResultSet rs) throws SQLException {
        Batiment batiment = new Batiment();
        batiment.setId(rs.getInt("id"));
        batiment.setNom(rs.getString("nom"));
        batiment.setLocalisation(rs.getString("localisation"));
        return batiment;
    }
}