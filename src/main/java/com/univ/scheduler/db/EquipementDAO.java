package com.univ.scheduler.db;

import com.univ.scheduler.model.Equipement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des équipements
 */
public class EquipementDAO implements DAO<Equipement> {

    private Connection connection;

    public EquipementDAO() {
        this.connection = DBConnection.getConnection();
    }

    @Override
    public Equipement getById(int id) {
        String sql = "SELECT * FROM Equipement WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractEquipementFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getById equipement: " + id);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Equipement> getAll() {
        List<Equipement> equipements = new ArrayList<>();
        String sql = "SELECT * FROM Equipement ORDER BY nom";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                equipements.add(extractEquipementFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll equipements");
            e.printStackTrace();
        }
        return equipements;
    }

    @Override
    public boolean insert(Equipement equipement) {
        String sql = "INSERT INTO Equipement (nom) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, equipement.getNom());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    equipement.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert equipement: " + equipement.getNom());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Equipement equipement) {
        String sql = "UPDATE Equipement SET nom = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, equipement.getNom());
            stmt.setInt(2, equipement.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update equipement: " + equipement.getId());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Equipement WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete equipement: " + id);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère les équipements d'une salle
     */
    public List<Equipement> getEquipementsBySalle(int salleId) {
        List<Equipement> equipements = new ArrayList<>();
        String sql = "SELECT e.* FROM Equipement e " +
                "JOIN Salle_Equipement se ON e.id = se.id_equipement " +
                "WHERE se.id_salle = ? ORDER BY e.nom";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                equipements.add(extractEquipementFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipements;
    }

    private Equipement extractEquipementFromResultSet(ResultSet rs) throws SQLException {
        Equipement equipement = new Equipement();
        equipement.setId(rs.getInt("id"));
        equipement.setNom(rs.getString("nom"));
        return equipement;
    }
}