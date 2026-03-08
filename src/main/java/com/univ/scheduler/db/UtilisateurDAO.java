package com.univ.scheduler.db;

import com.univ.scheduler.model.Utilisateur;  // ← IMPORT AJOUTÉ
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des utilisateurs
 */
public class UtilisateurDAO implements DAO<Utilisateur> {

    private Connection connection;

    public UtilisateurDAO() {
        this.connection = DBConnection.getConnection();
    }

    @Override
    public Utilisateur getById(int id) {
        String sql = "SELECT * FROM Utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUtilisateurFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getById utilisateur: " + id);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Utilisateur> getAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM Utilisateur ORDER BY nom";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                utilisateurs.add(extractUtilisateurFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll utilisateurs");
            e.printStackTrace();
        }
        return utilisateurs;
    }

    @Override
    public boolean insert(Utilisateur utilisateur) {
        String sql = "INSERT INTO Utilisateur (nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getEmail());
            stmt.setString(3, utilisateur.getMotDePasse());
            stmt.setString(4, utilisateur.getRole());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    utilisateur.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert utilisateur: " + utilisateur.getEmail());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Utilisateur utilisateur) {
        String sql = "UPDATE Utilisateur SET nom = ?, email = ?, role = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getEmail());
            stmt.setString(3, utilisateur.getRole());
            stmt.setInt(4, utilisateur.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update utilisateur: " + utilisateur.getId());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete utilisateur: " + id);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authentifie un utilisateur par email et mot de passe
     */
    public Utilisateur authenticate(String email, String password) {
        String sql = "SELECT * FROM Utilisateur WHERE email = ? AND mot_de_passe = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUtilisateurFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur authentification: " + email);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère les utilisateurs par rôle
     */
    public List<Utilisateur> getByRole(String role) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM Utilisateur WHERE role = ? ORDER BY nom";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                utilisateurs.add(extractUtilisateurFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getByRole: " + role);
            e.printStackTrace();
        }
        return utilisateurs;
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Utilisateur WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Utilisateur extractUtilisateurFromResultSet(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getInt("id"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setEmail(rs.getString("email"));
        utilisateur.setMotDePasse(rs.getString("mot_de_passe"));
        utilisateur.setRole(rs.getString("role"));
        return utilisateur;
    }
}